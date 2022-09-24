package com.lld.im.codec;

import com.lld.im.codec.utils.ByteBufToMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author: Chackylee
 * @description: 消息解码类
 * @create: 2022-04-27 16:45
 *
 **/
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List out) throws Exception {

        /** 如果可读的数据小于8表示没有数据*/
        if (in.readableBytes() < 20) {
            return;
        }

        out.add(ByteBufToMessageUtils.transition(in));
    }

}
