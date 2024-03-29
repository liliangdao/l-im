package com.lld.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lld.im.codec.pack.message.ChatMessageAck;
import com.lld.im.codec.pack.user.LoginAckPack;
import com.lld.im.codec.pack.user.UserStatusChangeNotifyPack;
import com.lld.im.codec.proto.Message;
import com.lld.im.codec.pack.user.LoginPack;
import com.lld.im.codec.proto.MessagePack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.UserPipelineConnectState;
import com.lld.im.common.enums.command.*;
import com.lld.im.common.model.UserClientDto;
import com.lld.im.common.model.UserSession;
import com.lld.im.common.model.msg.CheckSendMessageReq;
import com.lld.im.tcp.feign.FeignMessageService;
import com.lld.im.tcp.publish.MqMessageProducer;
import com.lld.im.tcp.redis.RedisManager;
import com.lld.im.tcp.utils.SessionSocketHolder;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeoutException;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/3/12
 * @version: 1.0
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<Message> {

    private final static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private String brokerId;

    private FeignMessageService feignMessageService;

    public NettyServerHandler(String brokerId,String url) {
        super();
        this.brokerId = brokerId;

        feignMessageService = Feign.builder()
                .encoder(new JacksonEncoder()) // 编码方式
                .decoder(new JacksonDecoder())  // 解码方式
                .options(new Request.Options(1000, 3500))//设置超时时间
                //.retryer(new Retryer.Default(5000, 5000, 3))
                .target(FeignMessageService.class,   //代理对象
                        url);//目标地址
    }

    /**
     * 读取数据
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws InterruptedException, IOException, TimeoutException {

        int command = msg.getMessageHeader().getCommand();

        if (command == SystemCommand.LOGIN.getCommand()) {
            LoginPack loginPack = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()),
                    new TypeReference<LoginPack>() {
                    }.getType());
//            LoginPack loginReq = loginPack.getData();
            logger.info("收到登录消息{}" , JSONObject.toJSONString(loginPack));

            /** 登陸事件 **/
            String userId = loginPack.getUserId();
            if(userId == null){
                ctx.channel().close();
                return;
            }

            /** 为channel设置用户id **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.UserId)).set(userId);
            String hashKey = msg.getMessageHeader().getClientType() + ":" + msg.getMessageHeader().getImei();
            /** 为channel设置client和imel **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.ClientImei)).set(hashKey);
            /** 为channel设置appId **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.AppId)).set(msg.getMessageHeader().getAppId());

            /** 为channel设置ClientType **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.ClientType))
                    .set(msg.getMessageHeader().getClientType());

            /** 为channel设置Imei **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.Imei))
                    .set(msg.getMessageHeader().getImei());

            // 设置userSession到redis
            UserSession session = new UserSession();
            session.setAppId(msg.getMessageHeader().getAppId());
            session.setClientType(msg.getMessageHeader().getClientType());
            session.setImei(msg.getMessageHeader().getImei());
            session.setUserId(loginPack.getUserId());
            session.setConnectState(UserPipelineConnectState.ONLINE.getCommand());
            try {
                InetAddress addr = InetAddress.getLocalHost();
                session.setPipelineHost(addr.getHostAddress());
                session.setMqRouteKey(brokerId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<Object, Object> map = redissonClient.getMap(msg.getMessageHeader().getAppId() + Constants.RedisConstants.UserSessionConstants + userId);
            map.put(hashKey, JSONObject.toJSONString(session));
            SessionSocketHolder.put(msg.getMessageHeader().getAppId(), userId,
                    msg.getMessageHeader().getClientType(), msg.getMessageHeader().getImei(), (NioSocketChannel) ctx.channel());

            // 通知其他端下线,例如：安卓与ios互斥，windows和mac互斥，是否允许多设备登录
            UserClientDto dto = new UserClientDto();
            dto.setAppId(msg.getMessageHeader().getAppId());
            dto.setClientType(msg.getMessageHeader().getClientType());
            dto.setImei(msg.getMessageHeader().getImei());
            dto.setUserId(loginPack.getUserId());
            RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);
            topic.publish(JSON.toJSONString(dto));

            UserStatusChangeNotifyPack pack = new UserStatusChangeNotifyPack();
            pack.setAppId(msg.getMessageHeader().getAppId());
            pack.setUserId(loginPack.getUserId());
            pack.setCustomStatus(loginPack.getCustomStatus());
            pack.setCustomText(loginPack.getCustomText());
            pack.setStatus(UserPipelineConnectState.ONLINE.getCommand());

            //发送在线状态修改信息-》通知用户
            MqMessageProducer.sendMessageByCommand(pack,msg.getMessageHeader(),UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());

            //返回给当前端登录成功 -> 仅代表和tcp服务连通
            MessagePack<LoginAckPack> loginSuccess = new MessagePack<>();
            LoginAckPack loginSuccessPack = new LoginAckPack();
            loginSuccessPack.setUserId(loginPack.getUserId());
            loginSuccess.setCommand(SystemCommand.LOGINACK.getCommand());
            loginSuccess.setData(loginSuccessPack);
            loginSuccess.setAppId(msg.getMessageHeader().getAppId());
            loginSuccess.setToId(loginPack.getUserId());
            ctx.writeAndFlush(loginSuccess);

        } else if (command == SystemCommand.LOGOUT.getCommand()) {
            /** 登出事件 **/
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());
        }
        else if (command == SystemCommand.PING.getCommand()) {
            ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime))
                    .set(System.currentTimeMillis());
        } else if (command == MessageCommand.MSG_P2P.getCommand()
                || command == GroupEventCommand.MSG_GROUP.getCommand()) {
            try {
                CheckSendMessageReq req = new CheckSendMessageReq();
                req.setCommand(command);
                req.setAppId(msg.getMessageHeader().getAppId());
                JSONObject jsonObject = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()));
                req.setFromId(jsonObject.getString("fromId"));
                if(command == MessageCommand.MSG_P2P.getCommand()){
                    req.setToId(jsonObject.getString("toId"));
                }else{
                    req.setToId(jsonObject.getString("groupId"));
                }
                ResponseVO responseVO = feignMessageService
                        .checkSendMessage(req);

                if(responseVO.isOk()){
                    MqMessageProducer.sendMessageByCommand(msg,command);
                }else{
                    Command cmd = null;
                    if(command == MessageCommand.MSG_P2P.getCommand()){
                        cmd = MessageCommand.MSG_ACK;
                    }else{
                        cmd = GroupEventCommand.GROUP_MSG_ACK;
                    }
                    ChatMessageAck ackData = new ChatMessageAck(jsonObject.getString("messageId"),
                            0);
                    MessagePack<ResponseVO> messageCheckAck = new MessagePack<>();
                    messageCheckAck.setCommand(cmd.getCommand());
                    messageCheckAck.setData(responseVO);
                    responseVO.setData(ackData);
                    ctx.channel().writeAndFlush(messageCheckAck);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (command == MessageCommand.MSG_READED.getCommand()) {
            try {
                MqMessageProducer.sendMessageByCommand(msg,command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //全往mq丢
            try{
                MqMessageProducer.sendMessageByCommand(msg,command);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //表示 channel 处于不活动状态
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //设置离线
        SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        //关闭通道
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        logger.info("有客户端上线了 ： {}",ctx.channel().id().asLongText());
    }


}
