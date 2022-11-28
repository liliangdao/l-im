package com.lld.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lld.im.codec.pack.user.LoginAckPack;
import com.lld.im.codec.pack.user.UserStatusChangeNotifyPack;
import com.lld.im.codec.proto.Message;
import com.lld.im.codec.pack.user.LoginPack;
import com.lld.im.codec.proto.MessagePack;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.UserPipelineConnectState;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.enums.command.SystemCommand;
import com.lld.im.common.enums.command.UserEventCommand;
import com.lld.im.common.model.UserClientDto;
import com.lld.im.common.model.UserSession;
import com.lld.im.tcp.publish.MqMessageProducer;
import com.lld.im.tcp.redis.RedisManager;
import com.lld.im.tcp.utils.SessionSocketHolder;
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

    public NettyServerHandler(String brokerId) {
        super();
        this.brokerId = brokerId;
    }

//    /**
//     * 数据读取完毕处理方法
//     *
//     * @param ctx
//     * @throws Exception
//     */
//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) {
//        ByteBuf buf = Unpooled.copiedBuffer("HelloClient".getBytes(CharsetUtil.UTF_8));
//        ctx.writeAndFlush(buf);
//    }

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
            SessionSocketHolder.put(msg.getMessageHeader().getAppId(), userId, msg.getMessageHeader().getClientType(), msg.getMessageHeader().getImei(), (NioSocketChannel) ctx.channel());

            // 通知其他端下线,例如：安卓与ios互斥，windows和mac互斥，是否允许多设备登录
            UserClientDto dto = new UserClientDto();
            dto.setAppId(msg.getMessageHeader().getAppId());
            dto.setClientType(msg.getMessageHeader().getClientType());
            dto.setImei(msg.getMessageHeader().getImei());
            dto.setUserId(loginPack.getUserId());
            RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);
            topic.publish(JSON.toJSONString(dto));

            //返回给当前端登录成功 -> 仅代表和tcp服务连通
            MessagePack<LoginAckPack> loginSuccess = new MessagePack<>();
            LoginAckPack loginSuccessPack = new LoginAckPack();
            loginSuccessPack.setUserId(loginPack.getUserId());
            loginSuccess.setCommand(SystemCommand.LOGINACK.getCommand());
            loginSuccess.setData(loginSuccessPack);
            loginSuccess.setAppId(msg.getMessageHeader().getAppId());
            loginSuccess.setToId(loginPack.getUserId());
            ctx.writeAndFlush(loginSuccess);

            UserStatusChangeNotifyPack pack = new UserStatusChangeNotifyPack();
            pack.setAppId(msg.getMessageHeader().getAppId());
            pack.setUserId(loginPack.getUserId());
            pack.setCustomStatus(loginPack.getCustomStatus());
            pack.setCustomText(loginPack.getCustomText());
            pack.setStatus(UserPipelineConnectState.ONLINE.getCommand());

            //发送在线状态修改信息-》通知用户
            MqMessageProducer.sendMessageByCommand(pack,msg.getMessageHeader(),UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY.getCommand());

        } else if (command == SystemCommand.LOGOUT.getCommand()) {
            /** 登出事件 **/
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());
        }
        else if (command == SystemCommand.PING.getCommand()) {
            //PING
            ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime))
                    .set(System.currentTimeMillis());
        } else if (command == MessageCommand.MSG_P2P.getCommand()) {
            try {
                MqMessageProducer.sendMessageByCommand(msg,command);
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
