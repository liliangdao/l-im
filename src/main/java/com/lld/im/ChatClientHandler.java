package com.lld.im;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ChatClientHandler extends SimpleChannelInboundHandler<String> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg){
        System.out.println(msg.trim());
    }

    public static void main(String[] args) {
        String property = System.getProperty("intercept.handler");
        System.out.println(property);
    }

}