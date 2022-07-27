package com.lld.im.tcp.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-06 09:19
 **/
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 判断evt是否是IdleStateEvent（用于触发用户事件，包含 读空闲/写空闲/读写空闲 ）
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent)evt;		// 强制类型转换
            if (event.state() == IdleState.READER_IDLE) {
//                System.out.println("进入读空闲...");
            } else if (event.state() == IdleState.WRITER_IDLE) {
//                System.out.println("进入写空闲...");
            } else if (event.state() == IdleState.ALL_IDLE) {
                ServerHeartBeatHandler.process(ctx);
            }
        }

    }

}
