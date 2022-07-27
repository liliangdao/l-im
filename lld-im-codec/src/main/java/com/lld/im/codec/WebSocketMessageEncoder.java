package com.lld.im.codec;


import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.proto.Message;
import com.lld.im.codec.proto.MessagePack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-07 08:51
 **/
public class WebSocketMessageEncoder extends MessageToMessageEncoder<Message> {

    private static Logger log = LoggerFactory.getLogger(WebSocketMessageEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, List<Object> out)  {

        try {
            Message msgBody = (Message) msg;
            Integer command = msgBody.getMessageHeader().getCommand();
            MessagePack body = msgBody.getMessagePack();

            String s = JSONObject.toJSONString(body);
            ByteBuf byteBuf = Unpooled.directBuffer(8+s.length());

            byte[] bytes = s.getBytes();
            byteBuf.writeInt(bytes.length);
            byteBuf.writeInt(command);
            byteBuf.writeBytes(bytes);
            out.add(new BinaryWebSocketFrame(byteBuf));
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}