package com.lld.im.handler;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.enums.MsgChatOperateType;
import com.lld.im.proto.Msg;
import com.lld.im.proto.MsgBody;
import com.lld.im.proto.MsgHeader;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/3/12
 * @version: 1.0
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<Msg> {

    private static ConcurrentHashMap<String, Channel> userManage = new ConcurrentHashMap<>();

    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

    //读取数据
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Msg msg) throws Exception {

        int command = msg.getMsgHeader().getCommand();

//        ctx.writeAndFlush("test");

        if(command == MsgChatOperateType.LOGIN.getCommand()){
            /** 登陸事件 **/
            String fromId = msg.getMsgBody().getFromId();
            /** 为channel设置用户id **/
            ctx.channel().attr(AttributeKey.valueOf("userId")).set(fromId);
            //TODO 设置userSession到redis

            //TODO end
            userManage.put(fromId,ctx.channel());
        }else if(command == MsgChatOperateType.LOGOUT.getCommand()){
            /** 登出事件 **/
//            String fromId = msgObject.getString("fromId");

        }else{
            /** 测试*/
            String toId = msg.getMsgBody().getToId();
            Channel channel = userManage.get(toId);
            if(channel == null){

                Msg sendPack = new Msg();
                MsgBody body = new MsgBody();
                body.setFromId(msg.getMsgBody().getFromId());
                body.setMsgBody("mubiaobuzaixian");

                MsgHeader header = new MsgHeader();
                header.setCommand(0x44F);
                sendPack.setMsgHeader(header);
                sendPack.setMsgBody(body);

                ctx.channel().writeAndFlush(sendPack);
            }else{

                Msg sendPack = new Msg();
                MsgBody body = new MsgBody();
                body.setFromId(msg.getMsgBody().getFromId());
                body.setMsgBody(msg.getMsgBody().getMsgBody().toString());

                MsgHeader header = new MsgHeader();
                header.setCommand(0x44F);
                sendPack.setMsgHeader(header);
                sendPack.setMsgBody(body);
                ctx.channel().writeAndFlush(sendPack);
//                channel.writeAndFlush(sendPack);
            }

        }



    }

    //表示 channel 处于不活动状态, 提示离线了
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        channelGroup.writeAndFlush("[ 客户端 ]" + ctx.channel().remoteAddress() + "退出了群聊");
        System.out.println(ctx.channel().remoteAddress() + " 下线了" + "\n");
        System.out.println("channelGroup size=" + channelGroup.size());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //关闭通道
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("上线");
    }
}
