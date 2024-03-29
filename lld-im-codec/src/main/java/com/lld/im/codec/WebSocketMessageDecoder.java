package com.lld.im.codec;

import com.lld.im.codec.proto.Message;
import com.lld.im.codec.utils.ByteBufToMessageUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-06 16:24
 **/
@ChannelHandler.Sharable
public class WebSocketMessageDecoder extends MessageToMessageDecoder<BinaryWebSocketFrame> {
    
    @Override
    protected void decode(ChannelHandlerContext ctx, BinaryWebSocketFrame frame, List<Object> out) throws Exception {

        ByteBuf in = frame.content();
        if (in.readableBytes() < 24) {
            return;
        }
        Message message = ByteBufToMessageUtils.transition(in);
        if(message == null){
            return;
        }
        out.add(message);
    }

}
