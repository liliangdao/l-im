package com.lld.im.codec;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.enums.MsgCommand;
import com.lld.im.proto.Msg;
import com.lld.im.proto.MsgBody;
import com.lld.im.proto.MsgHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.commons.lang3.CharSet;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author: Chackylee
 * @description: 消息解码类
 * @create: 2022-04-27 16:45
 *
 * 私有协议规则，前4位表示数据长度，接着是command 4位，后面表示数据，后续将解码方式加到数据头根据不同的解码方式解码，如pb，json，现在用json字符串
 **/
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List out) throws Exception {

        /** 如果可读的数据小于8表示没有数据，4位表示数据长度4位表示command，不做处理*/
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
