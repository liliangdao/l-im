package com.lld.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lld.im.codec.pack.MessageReadedPack;
import com.lld.im.codec.pack.UserStatusChangeNotifyPack;
import com.lld.im.codec.proto.Message;
import com.lld.im.codec.pack.LoginPack;
import com.lld.im.codec.proto.MessagePack;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ImConnectStatusEnum;
import com.lld.im.common.enums.UserPipelineConnectState;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.enums.command.SystemCommand;
import com.lld.im.common.enums.command.UserEventCommand;
import com.lld.im.common.model.UserClientDto;
import com.lld.im.common.model.UserSession;
import com.lld.im.common.model.msg.MessageReadedContent;
import com.lld.im.tcp.publish.MqMessageProducer;
import com.lld.im.tcp.redis.RedisManager;
import com.lld.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import jodd.bean.BeanUtil;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
            /** 为channel设置用户id **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.UserId)).set(userId);
            String hashKey = loginPack.getClientType() + ":" + loginPack.getImei();
            /** 为channel设置client和imel **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.ClientImei)).set(hashKey);
            /** 为channel设置appId **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.AppId)).set(loginPack.getAppId());

            // 设置userSession到redis
            UserSession session = new UserSession();
            session.setAppId(loginPack.getAppId());
            session.setClientType(loginPack.getClientType());
            session.setImei(loginPack.getImei());
            session.setUserId(loginPack.getUserId());
            session.setConnectState(ImConnectStatusEnum.ONLINE_STATUS.getCode());
            try {
                InetAddress addr = InetAddress.getLocalHost();
                session.setPipelineHost(addr.getHostAddress());
                session.setMqRouteKey(brokerId);
            } catch (Exception e) {
                e.printStackTrace();
            }

            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<Object, Object> map = redissonClient.getMap(loginPack.getAppId() + Constants.RedisConstants.UserSessionConstants + userId);
            map.put(hashKey, JSONObject.toJSONString(session));
            SessionSocketHolder.put(loginPack.getAppId(), userId, loginPack.getClientType(), loginPack.getImei(), (NioSocketChannel) ctx.channel());

            // 通知其他端下线,例如：安卓与ios互斥，windows和mac互斥，是否允许多设备登录
            UserClientDto dto = new UserClientDto();
            dto.setAppId(loginPack.getAppId());
            dto.setClientType(loginPack.getClientType());
            dto.setImei(loginPack.getImei());
            dto.setUserId(loginPack.getUserId());
            RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);
            topic.publish(JSON.toJSONString(dto));
//            stringRedisTemplate.convertAndSend(Constants.RedisConstants.UserLoginChannel, JSON.toJSONString(dto));

            JSONObject loginSuccessPack = new JSONObject();
            loginSuccessPack.put("code",200);
//            loginSuccessPack.put("command",SystemCommand.LOGIN.getCommand());
            MessagePack<JSONObject> loginSuccess = new MessagePack<>();
            loginSuccess.setData(loginSuccessPack);
            loginSuccess.setCommand(SystemCommand.LOGIN.getCommand());
            ctx.writeAndFlush(loginSuccess);

            //发送mq,用户在线状态修改
            MqMessageProducer.sendMessageByCommand(msg.getMessagePack(), UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand());

            UserStatusChangeNotifyPack pack = new UserStatusChangeNotifyPack();
            Collection<Object> values = map.values();
            List<UserSession> userSessions = new ArrayList<>();
            for (Object value : values) {
                UserSession userSession = JSONObject.parseObject(value.toString(), UserSession.class);
                userSessions.add(userSession);
            }
            pack.setClient(userSessions);
            pack.setAppId(loginPack.getAppId());
            pack.setUserId(loginPack.getUserId());
            pack.setClientType(loginPack.getClientType());
            pack.setCustomStatus(loginPack.getCustomStatus());
            pack.setCustomText(loginPack.getCustomText());
            pack.setStatus(UserPipelineConnectState.ONLINE.getCommand());

            //发送在线状态修改信息-》通知用户
            MqMessageProducer.sendMessageByCommand(pack,UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY.getCommand());

        } else if (command == SystemCommand.LOGOUT.getCommand()) {
            /** 登出事件 **/
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());
            /** TODO 去推送服务删除掉推送信息 */
        }
        else if (command == SystemCommand.PING.getCommand()) {
            //PING

        } else if (command == MessageCommand.MSG_P2P.getCommand()) {
            try {
                MqMessageProducer.sendMessageByCommand(msg.getMessagePack(),command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (command == MessageCommand.MSG_READED.getCommand()) {
            try {
                MessageReadedPack pack = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()), new TypeReference<MessageReadedPack>() {
                }.getType());
                MessageReadedContent content = new MessageReadedContent();
                content.setConversationType(pack.getConversationType());
                content.setFromId(pack.getFromId());
                content.setMessageSequence(pack.getMessageSequence());
                content.setToId(pack.getToId());
                content.setAppId(pack.getAppId());
                content.setImei(pack.getImei());
                content.setClientType(pack.getClientType());
                MqMessageProducer.sendMessageByCommand(content,command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //全往mq丢
            try{
                MqMessageProducer.sendMessageByCommand(msg.getMessagePack(),command);
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
        //关闭通道
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        logger.info("有客户端上线了 ： {}",ctx.channel().id().asLongText());
    }
}
