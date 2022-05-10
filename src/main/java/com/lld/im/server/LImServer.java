package com.lld.im.server;

import com.lld.im.codec.MessageDecoder;
import com.lld.im.codec.MessageEncoder;
import com.lld.im.handler.HeartBeatHandler;
import com.lld.im.handler.NettyServerHandler;
import com.lld.im.proto.Msg;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
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
public class LImServer {

    private final static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    @Value("${tcpPort}")
    private Integer tcpPort;

    private EventLoopGroup mainGroup;
    private EventLoopGroup subGroup;
    private ServerBootstrap server;
    private ChannelFuture future;

    private static class SingletionLImServer {
        static final LImServer instance = new LImServer();
    }

    public static LImServer getInstance() {
        return SingletionLImServer.instance;
    }

    public LImServer(){
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
                        //加入自定义包解码器
                        pipeline.addLast("decoder", new MessageDecoder());
                        //向pipeline加入编码器
                        pipeline.addLast("encoder", new MessageEncoder(Msg.class));
                        //心跳检测Handler
                        pipeline.addLast(new IdleStateHandler(8, 10, 12));
                        // 自定义的空闲状态检测
                        pipeline.addLast(new HeartBeatHandler());
                        //加入自己的业务处理handler
                        pipeline.addLast(new NettyServerHandler());
                    }
                });


    }

    public void start(){
        this.future = server.bind(tcpPort);
        logger.info("tcp server start success");
    }


}
