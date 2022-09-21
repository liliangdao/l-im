package com.lld.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lld.im.codec.pack.MessageReadedPack;
import com.lld.im.codec.proto.Message;
import com.lld.im.codec.proto.MessagePack;
import com.lld.im.codec.proto.MessageHeader;
import com.lld.im.codec.pack.LoginPack;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ImConnectStatusEnum;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.enums.command.SystemCommand;
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

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.util.List;

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
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws InterruptedException {

        int command = msg.getMessageHeader().getCommand();

        if (command == SystemCommand.LOGIN.getCommand()) {

            MessagePack<LoginPack> loginPack = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()), new TypeReference<MessagePack<LoginPack>>() {
            }.getType());
            LoginPack loginReq = loginPack.getData();

            /** 登陸事件 **/
            String userId = msg.getMessagePack().getUserId();
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
            session.setUserId(loginReq.getUserId());
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
            dto.setUserId(loginReq.getUserId());
            RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);
            topic.publish(JSON.toJSONString(dto));
//            stringRedisTemplate.convertAndSend(Constants.RedisConstants.UserLoginChannel, JSON.toJSONString(dto));

        } else if (command == SystemCommand.LOGOUT.getCommand()) {
            /** 登出事件 **/
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());
            /** TODO 去推送服务删除掉推送信息 */
        } else if (command == MessageCommand.TEST.getCommand()) {
            /** 测试Data里面是字符串 */
            String toId = msg.getMessagePack().getToId();
            List<NioSocketChannel> nioSocketChannels = SessionSocketHolder.get(msg.getMessagePack().getAppId(), toId);
            if (nioSocketChannels.isEmpty()) {
                Message sendPack = new Message();
                MessagePack body = new MessagePack();
                body.setUserId("system");
                body.setToId(msg.getMessagePack().getUserId());
                body.setData("目标不在线");
                MessageHeader header = new MessageHeader();
                header.setCommand(0x44F);
                sendPack.setMessageHeader(header);
                sendPack.setMessagePack(body);
                ctx.channel().writeAndFlush(sendPack);
            } else {

                Message sendPack = new Message();
                MessagePack body = new MessagePack();
                body.setUserId(msg.getMessagePack().getUserId());
                body.setToId(msg.getMessagePack().getToId());
                body.setData(msg.getMessagePack().getData().toString());

                MessageHeader header = new MessageHeader();
                header.setCommand(0x44F);
                sendPack.setMessageHeader(header);
                sendPack.setMessagePack(body);

                nioSocketChannels.forEach(c -> {
                    c.writeAndFlush(sendPack);
                });
                ctx.channel().writeAndFlush(sendPack);
            }
        } else if (command == SystemCommand.PING.getCommand()) {
            //PING

        } else if (command == MessageCommand.MSG_P2P.getCommand()) {
            try {
                MqMessageProducer.sendMessageToMessageService(msg.getMessagePack().getData(),command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else if (command == MessageCommand.MSG_READED.getCommand()) {
            try {
                MessagePack<MessageReadedPack> pack = JSON.parseObject(JSONObject.toJSONString(msg.getMessagePack()), new TypeReference<MessagePack<MessageReadedPack>>() {
                }.getType());
                MessageReadedContent content = new MessageReadedContent();
                content.setConversationType(pack.getData().getConversationType());
                content.setFromId(pack.getData().getFromId());
                content.setMessageSequence(pack.getData().getMessageSequence());
                content.setToId(pack.getToId());
                content.setAppId(pack.getAppId());
                content.setImei(pack.getImei());
                content.setClientType(pack.getClientType());
                MqMessageProducer.sendMessageToMessageService(content,command);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            //全往mq丢
            try{
                MqMessageProducer.sendMessageToGroupService(msg.getMessagePack().getData(),command);
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
