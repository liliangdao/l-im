package com.lld.im.codec.utils;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.proto.Message;
import com.lld.im.codec.proto.MessageHeader;
import io.netty.buffer.ByteBuf;

import java.io.UnsupportedEncodingException;

/**
 * @author: Chackylee
 * @description: 将ByteBuf转化为Message实体，根据私有协议转换
 *               私有协议规则
 *               4位表示Command表示消息的开始，
 *               4位表示version
 *               4位表示clientType
 *               4位表示messageType
 *               4位表示appId
 *               4位表示imei长度
 *               4位表示数据长度
 *               imei
 *               data
 *               后续将解码方式加到数据头根据不同的解码方式解码，如pb，json，现在用json字符串
 * @create: 2022-05-10 10:22
 **/
public class ByteBufToMessageUtils {

    public static Message transition(ByteBuf byteBuf) throws UnsupportedEncodingException {

        /** 获取command*/
        int command = byteBuf.readInt();

        /** 获取version*/
        int version = byteBuf.readInt();

        /** 端类型*/
        int clientType = byteBuf.readInt();

        /** 消息用什么方式解析，不是真正的消息类型*/
        int messageType = byteBuf.readInt();

        /** appid*/
        int appId = byteBuf.readInt();

        /** imei Length*/
        int imeiLength = byteBuf.readInt();
        byte[] imeiData = new byte[imeiLength];

        /** data Length*/
        int dataLength = byteBuf.readInt();
        byte[] data = new byte[dataLength];

        if(byteBuf.readableBytes() < dataLength + imeiLength){
            byteBuf.resetReaderIndex();
            return null;
        }

        byteBuf.readBytes(imeiData);
        byteBuf.readBytes(data);

        /** 填充数据头*/
        MessageHeader msgHeader = new MessageHeader();
        msgHeader.setCommand(command);
        msgHeader.setAppId(appId);
        msgHeader.setVersion(version);
        msgHeader.setClientType(clientType);
        msgHeader.setLength(dataLength);
        msgHeader.setImei(new String(imeiData));
        msgHeader.setMessageType(messageType);

        Object body = null;

        if(messageType == 0x0){
            /** 将字节数组转为string*/
            String msgString = new String(data,"utf-8");
            /** 将json字符串转为jsonObject*/
            JSONObject o = (JSONObject) JSONObject.parse(msgString);
            /** 填充MessagePack*/
//            body = o.toJavaObject(MessagePack.class);
            body = o;
        }

        /** 完整的Msg*/
        Message message = new Message();
        message.setMessageHeader(msgHeader);
        message.setMessagePack(body);

        byteBuf.markReaderIndex();
        return message;
    }

}
