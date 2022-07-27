package com.lld.im.tcp.handler;

import com.lld.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author: Chackylee
 * @description: 真正处理心跳超时的类
 * @create: 2022-05-06 09:22
 **/
public class ServerHeartBeatHandler {
    /**
     * @description 处理用户心跳超时,设置session的connectState为离线
     * @author chackylee
     * @date 2022/5/6 9:23
     * @param [ctx]
     * @return void
    */
    public static void process(ChannelHandlerContext ctx){
//        String longId = ctx.channel().id().asLongText();
//        System.out.println(longId + " 心跳超时");
        SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
    }

}
