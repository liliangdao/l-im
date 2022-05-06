package com.lld.im.codec;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.proto.Msg;
import com.lld.im.proto.MsgBody;
import com.lld.im.proto.MsgHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
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
        if (in.readableBytes() < 8) {
            return;
        }

        /** 获取数据长度*/
        int dataLength = in.readInt();
        /** 获取command*/
        int command = in.readInt();

        byte[] data = new byte[dataLength];
        in.readBytes(data);

        /** 将字节数组转为string*/
        String msg = new String(data,"utf-8");
        /** 将json字符串转为jsonObject*/
        JSONObject o = (JSONObject) JSONObject.parse(msg);

        /** 填充数据头*/
        MsgHeader msgHeader = new MsgHeader();
        msgHeader.setCommand(command);
        msgHeader.setLength(dataLength);

        /** 填充msgBody*/
        MsgBody body = o.toJavaObject(MsgBody.class);
        /** 完整的Msg*/
        Msg msgBody = new Msg();
        msgBody.setMsgHeader(msgHeader);
        msgBody.setMsgBody(body);

        out.add(msgBody);

    }




}
