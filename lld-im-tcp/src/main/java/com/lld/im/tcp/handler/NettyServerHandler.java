package com.lld.im.tcp.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.proto.Msg;
import com.lld.im.codec.proto.MsgBody;
import com.lld.im.codec.proto.MsgHeader;
import com.lld.im.codec.pack.LoginMsg;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.MessageCommand;
import com.lld.im.common.model.UserClientDto;
import com.lld.im.common.model.UserSession;
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

import java.net.InetAddress;
import java.util.List;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/3/12
 * @version: 1.0
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<Msg> {

    private final static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

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

    /** 读取数据*/
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Msg msg) throws InterruptedException {

        int command = msg.getMsgHeader().getCommand();

        if(command == MessageCommand.LOGIN.getCommand()){

            LoginMsg loginReq = JSONObject.parseObject(msg.getMsgBody().getData().toString(), LoginMsg.class);
            /** 登陸事件 **/
            String userId = msg.getMsgBody().getUserId();
            /** 为channel设置用户id **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.UserId)).set(userId);
            String hashKey = loginReq.getClientType()+":"+loginReq.getImei();
            /** 为channel设置client和imel **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.ClientImei)).set(hashKey);
            /** 为channel设置appId **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.AppId)).set(loginReq.getAppId());

            // 设置userSession到redis
            UserSession session = new UserSession();
            session.setAppId(loginReq.getAppId());
            session.setClientType(loginReq.getClientType());
            session.setImei(loginReq.getImei());
            session.setUserId(loginReq.getUserId());
            try{
                InetAddress addr = InetAddress.getLocalHost();
                session.setPipelineHost(addr.getHostAddress());
            }catch (Exception e){
                e.printStackTrace();
            }

            RedissonClient redissonClient = RedisManager.getRedissonClient();
            RMap<Object, Object> map = redissonClient.getMap(loginReq.getAppId() + Constants.RedisConstants.UserSessionConstants + userId);
            map.put(hashKey,JSONObject.toJSONString(session));
            SessionSocketHolder.put(loginReq.getAppId(),userId,loginReq.getClientType(),loginReq.getImei(), (NioSocketChannel) ctx.channel());

            // 通知其他端下线,例如：安卓与ios互斥，windows和mac互斥，是否允许多设备登录
            UserClientDto dto = new UserClientDto();
            dto.setAppId(loginReq.getAppId());
            dto.setClientType(loginReq.getClientType());
            dto.setImei(loginReq.getImei());
            dto.setUserId(loginReq.getUserId());
            RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);
            topic.publish(JSON.toJSONString(dto));
//            stringRedisTemplate.convertAndSend(Constants.RedisConstants.UserLoginChannel, JSON.toJSONString(dto));

        }else if(command == MessageCommand.LOGOUT.getCommand()){
            /** 登出事件 **/
            SessionSocketHolder.removeUserSession((NioSocketChannel) ctx.channel());
            /** TODO 去推送服务删除掉推送信息 */
        }else if(command == MessageCommand.TEST.getCommand()){
            /** 测试Data里面是字符串 */
            String toId = msg.getMsgBody().getToId();
            List<NioSocketChannel> nioSocketChannels = SessionSocketHolder.get(msg.getMsgBody().getAppId(), toId);
            if(nioSocketChannels.isEmpty()){
                Msg sendPack = new Msg();
                MsgBody body = new MsgBody();
                body.setUserId("system");
                body.setToId(msg.getMsgBody().getUserId());
                body.setData("目标不在线");
                MsgHeader header = new MsgHeader();
                header.setCommand(0x44F);
                sendPack.setMsgHeader(header);
                sendPack.setMsgBody(body);
                ctx.channel().writeAndFlush(sendPack);
            } else{

                Msg sendPack = new Msg();
                MsgBody body = new MsgBody();
                body.setUserId(msg.getMsgBody().getUserId());
                body.setToId(msg.getMsgBody().getToId());
                body.setData(msg.getMsgBody().getData().toString());

                MsgHeader header = new MsgHeader();
                header.setCommand(0x44F);
                sendPack.setMsgHeader(header);
                sendPack.setMsgBody(body);

                nioSocketChannels.forEach(c ->{
                    c.writeAndFlush(sendPack);
                });
                 ctx.channel().writeAndFlush(sendPack);
            }
        } else if(command == MessageCommand.PING.getCommand()){


        } else {
            //全往mq丢

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
