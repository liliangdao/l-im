package com.lld.im.server;

import com.lld.im.codec.WebSocketMessageDecoder;
import com.lld.im.codec.WebSocketMessageEncoder;
import com.lld.im.handler.NettyServerHandler;
import com.lld.im.handler.NettyWebSocketServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-05 14:20
 **/
@Component
public class LImWebSocketServer {


    @Value("${webSocketPort}")
    private Integer webSocketPort;

    private final static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private EventLoopGroup mainGroup;
    private EventLoopGroup subGroup;
    private ServerBootstrap server;
    private ChannelFuture future;

    private static class SingletionLImServer {
        static final LImWebSocketServer instance = new LImWebSocketServer();
    }

    public static LImWebSocketServer getInstance() {
        return SingletionLImServer.instance;
    }

    public LImWebSocketServer(){
        mainGroup = new NioEventLoopGroup(1);// 处理客户端连接请求数
        subGroup = new NioEventLoopGroup(8);// 真正服务的请求线程数(不填默认是cpu核心数2倍)
        server = new ServerBootstrap();
        server.group(mainGroup, subGroup)
                .channel(NioServerSocketChannel.class) //NioDatagramChannel.class 如果是udp使用这个类 下面设置的option也会不一样
                //简单说：option主要是针对boss线程组，child主要是针对worker线程组
                .option(ChannelOption.SO_BACKLOG, 10240) // 服务端可连接队列大小
                .option(ChannelOption.SO_REUSEADDR, true) // 参数表示允许重复使用本地地址和端口
                .childOption(ChannelOption.TCP_NODELAY, true) // 是否禁用Nagle算法 简单点说是否批量发送数据 true关闭 false开启。 开启的话可以减少一定的网络开销，但影响消息实时性
                .childOption(ChannelOption.SO_KEEPALIVE, true) // 保活开关2h没有数据服务端会发送心跳包

                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        // websocket 基于http协议，所以要有http编解码器
                        pipeline.addLast("http-codec",new HttpServerCodec());
                        // 对写大数据流的支持
                        pipeline.addLast("http-chunked",new ChunkedWriteHandler());
                        // 几乎在netty中的编程，都会使用到此hanler
                        pipeline.addLast("aggregator",new HttpObjectAggregator(65535));
                        /**
                         * websocket 服务器处理的协议，用于指定给客户端连接访问的路由 : /ws
                         * 本handler会帮你处理一些繁重的复杂的事
                         * 会帮你处理握手动作： handshaking（close, ping, pong） ping + pong = 心跳
                         * 对于websocket来讲，都是以frames进行传输的，不同的数据类型对应的frames也不同
                         */
                        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
                        //加入自定义包编解码器
                        pipeline.addLast(new WebSocketMessageDecoder());
                        pipeline.addLast(new WebSocketMessageEncoder());

                        pipeline.addLast(new NettyServerHandler());
                    }
                });

    }

    public void start(){
        this.future = server.bind(webSocketPort);
        logger.info("webSocket server start success");
    }


}


