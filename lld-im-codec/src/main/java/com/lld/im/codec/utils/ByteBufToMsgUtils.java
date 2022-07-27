package com.lld.im.codec.utils;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.proto.Message;
import com.lld.im.codec.proto.MessagePack;
import com.lld.im.codec.proto.MessageHeader;
import io.netty.buffer.ByteBuf;

import java.io.UnsupportedEncodingException;

/**
 * @author: Chackylee
 * @description: 将ByteBuf转化为Msg实体，根据私有协议转换
 *               私有协议规则，前4位表示数据长度，接着是command 4位，后面表示数据，后续将解码方式加到数据头根据不同的解码方式解码，如pb，json，现在用json字符串
 * @create: 2022-05-10 10:22
 **/
public class ByteBufToMsgUtils {

    public static Message transition(ByteBuf byteBuf) throws UnsupportedEncodingException {

        /** 获取数据长度*/
        int dataLength = byteBuf.readInt();
        /** 获取command*/
        int command = byteBuf.readInt();

        byte[] data = new byte[dataLength];
        byteBuf.readBytes(data);

        /** 将字节数组转为string*/
        String msgString = new String(data,"utf-8");
        /** 将json字符串转为jsonObject*/
        JSONObject o = (JSONObject) JSONObject.parse(msgString);

        /** 填充数据头*/
        MessageHeader msgHeader = new MessageHeader();
        msgHeader.setCommand(command);
        msgHeader.setLength(dataLength);

        /** 填充MessagePack*/
        MessagePack body = o.toJavaObject(MessagePack.class);
        /** 完整的Msg*/
        Message message = new Message();
        message.setMessageHeader(msgHeader);
        message.setMessagePack(body);

        return message;
    }

}
