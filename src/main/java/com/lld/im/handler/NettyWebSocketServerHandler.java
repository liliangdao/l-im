package com.lld.im.handler;

import org.springframework.stereotype.Component;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-06 15:32
 **/
//@Component
public class NettyWebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketServerHandshaker handshaker;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        try{
            //http请求和tcp请求分开处理
            if(msg instanceof HttpRequest){
                //连接后的处理
                handlerHttpRequest(ctx,(HttpRequest) msg);
            }else{
                //其他的操作交给下面的Handler操作去做
                ctx.fireChannelRead(msg);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * wetsocket第一次连接握手
     * @param ctx
     * @param msg
     */
    @SuppressWarnings("deprecation")
    private void handlerHttpRequest(ChannelHandlerContext ctx, HttpRequest req) {

        // http 解码失败,如果请求头不是升级到websocket，则响应403
        if(!req.getDecoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))){
            sendHttpResponse(ctx, (FullHttpRequest) req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.BAD_REQUEST));
        }
        //可以通过url获取其他参数
        WebSocketServerHandshakerFactory factory =
                new WebSocketServerHandshakerFactory("ws://"+req.headers().get("Host")+"/"+req.getUri()+"",null,false
                );

        handshaker = factory.newHandshaker(req);

        Channel channel = ctx.channel();
        if(handshaker == null){
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(channel);
        }else{
            //进行连接,握手
            handshaker.handshake(channel, req);
            //TODO

            //添加监听事件
            channel.newSucceededFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if (future.isSuccess()) {

                    }
                }

            });
        }
    }

    @SuppressWarnings("deprecation")
    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        // 返回应答给客户端
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
//            buf.release();
        }
        // 如果是非Keep-Alive，关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

}
