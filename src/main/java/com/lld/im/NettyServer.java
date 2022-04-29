package com.lld.im;

import com.lld.im.codec.MessageDecoder;
import com.lld.im.codec.MessageEncoder;
import com.lld.im.handler.NettyServerHandler;
import com.lld.im.proto.Msg;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/3/12
 * @version: 1.0
 */
public class NettyServer {

//    private final static Logger LOGGER = LoggerFactory.getLogger(NettyServer.class);
//    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    public static void main(String[] args) throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);// 处理客户端连接请求数
        EventLoopGroup workerGroup = new NioEventLoopGroup(8);// 真正服务的请求线程数
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
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
                            //加入特殊分隔符分包解码器
                            //pipeline.addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer("_"
                            // .getBytes())));
                            //向pipeline加入解码器
//                            pipeline.addLast("decoder", new NettyMessageDecoder(Integer.MAX_VALUE,0,4));
//                            pipeline.addLast("decoder", new NettyMessageDecoder(1024*1024*5,0,2,
//                                    0,0));
                            pipeline.addLast("decoder", new MessageDecoder());
                            //向pipeline加入编码器
//                            pipeline.addLast("encoder", new StringEncoder());
                            pipeline.addLast("encoder", new MessageEncoder(Msg.class));
                            //加入自己的业务处理handler
                            pipeline.addLast(new NettyServerHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind(9000).sync();
            //关闭通道
            channelFuture.channel().closeFuture().sync();
            if (channelFuture.isSuccess()) {
//                logger.info("Start tim server success...");
            }
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
