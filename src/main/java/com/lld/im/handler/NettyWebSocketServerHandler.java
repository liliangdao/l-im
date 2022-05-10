package com.lld.im.handler;

import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

import io.netty.channel.ChannelHandlerContext;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-06 15:32
 **/
public class NettyWebSocketServerHandler extends ChannelInboundHandlerAdapter {


    /*
    客户端接收到的CloseEvent事件类型
    客户端可通过该code判断是握手鉴权失败
     */
    private static final int UNAUTHORIZED_CODE = 4001;

    public NettyWebSocketServerHandler() {
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        super.userEventTriggered(ctx, evt);

        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            doAuthentication(ctx, (WebSocketServerProtocolHandler.HandshakeComplete) evt);
        }

    }

    private void doAuthentication(ChannelHandlerContext context, WebSocketServerProtocolHandler.HandshakeComplete event) {

//        if (handshakePredicate == null) {
//            return;
//        }

        /*
         * 鉴权不通过，关闭链接
         */
//        if (!handshakePredicate.test(HandshakeEvent.of(event))) {
//            context.channel().writeAndFlush(new CloseWebSocketFrame(UNAUTHORIZED_CODE,HttpResponseStatus.UNAUTHORIZED.reasonPhrase())).addListener(ChannelFutureListener.CLOSE);
//        }
    }

}


