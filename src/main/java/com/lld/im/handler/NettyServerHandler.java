package com.lld.im.handler;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.constant.Constants;
import com.lld.im.enums.MsgCommand;
import com.lld.im.model.AccountSession;
import com.lld.im.model.req.LoginMsg;
import com.lld.im.proto.Msg;
import com.lld.im.proto.MsgBody;
import com.lld.im.proto.MsgHeader;
import com.lld.im.utils.SessionSocketHolder;
import com.lld.im.utils.SpringBeanFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;

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
    protected void channelRead0(ChannelHandlerContext ctx, Msg msg) {

        int command = msg.getMsgHeader().getCommand();

        if(command == MsgCommand.LOGIN.getCommand()){

            LoginMsg loginReq = JSONObject.parseObject(msg.getMsgBody().getData().toString(), LoginMsg.class);
            /** 登陸事件 **/
            String userId = msg.getMsgBody().getUserId();
            /** 为channel设置用户id **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.UserId)).set(userId);
            String hashKey = loginReq.getClientType()+":"+loginReq.getImei();
            /** 为channel设置client和imel **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.ClientImel)).set(hashKey);
            /** 为channel设置appId **/
            ctx.channel().attr(AttributeKey.valueOf(Constants.AppId)).set(loginReq.getAppId());

            // 设置userSession到redis
            AccountSession accountSession = new AccountSession(loginReq);
            StringRedisTemplate stringRedisTemplate = SpringBeanFactory.getBean(StringRedisTemplate.class);
            stringRedisTemplate.opsForHash().put(loginReq.getAppId()+":"+ Constants.RedisConstants.accountSessionConstants+":"+userId,
                    hashKey,JSONObject.toJSONString(accountSession));
            SessionSocketHolder.put(loginReq.getAppId(),userId, (NioSocketChannel) ctx.channel());

        }else if(command == MsgCommand.LOGOUT.getCommand()){
            /** 登出事件 **/
            SessionSocketHolder.removeAccountSession((NioSocketChannel) ctx.channel());
            /** TODO 去推送服务删除掉推送信息 */
        }else if(command == MsgCommand.PING.getCommand()){

        }else{
            /** 测试Data里面是字符串 */
            String toId = msg.getMsgBody().getToId();
            NioSocketChannel channel = SessionSocketHolder.get(msg.getMsgBody().getAppId(),toId);
            if(channel == null){
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
            }else{

                Msg sendPack = new Msg();
                MsgBody body = new MsgBody();
                body.setUserId(msg.getMsgBody().getUserId());
                body.setToId(msg.getMsgBody().getToId());
                body.setData(msg.getMsgBody().getData().toString());

                MsgHeader header = new MsgHeader();
                header.setCommand(0x44F);
                sendPack.setMsgHeader(header);
                sendPack.setMsgBody(body);
                channel.writeAndFlush(sendPack);
                ctx.channel().writeAndFlush(sendPack);
            }
        }
    }

    //表示 channel 处于不活动状态
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        SessionSocketHolder.remove((NioSocketChannel) ctx.channel());
    }

//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//        //关闭通道
//        ctx.close();
//    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        System.out.println("上线");
//        logger.info("有客户端上线了 ： {}",ctx.channel().id().asLongText());
    }
}
