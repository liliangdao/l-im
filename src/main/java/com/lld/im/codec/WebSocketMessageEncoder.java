package com.lld.im.codec;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.proto.Msg;
import com.lld.im.proto.MsgBody;
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
public class WebSocketMessageEncoder extends MessageToMessageEncoder<Msg> {

    private static Logger log = LoggerFactory.getLogger(WebSocketMessageEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Msg msg, List<Object> out) throws Exception {

        Msg msgBody = (Msg) msg;
        Integer command = msgBody.getMsgHeader().getCommand();
        MsgBody body = msgBody.getMsgBody();

        String s = JSONObject.toJSONString(body);
        ByteBuf byteBuf = Unpooled.directBuffer(8+s.length());

        byte[] bytes = s.getBytes();
        byteBuf.writeInt(bytes.length);
        byteBuf.writeInt(command);
        byteBuf.writeBytes(bytes);
        out.add(new BinaryWebSocketFrame(byteBuf));
    }
}