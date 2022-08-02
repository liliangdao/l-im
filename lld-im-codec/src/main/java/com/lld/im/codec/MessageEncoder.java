package com.lld.im.codec;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.proto.Message;
import com.lld.im.codec.proto.MessagePack;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author: Chackylee
 * @description: 消息编码类，私有协议规则，前4位表示长度，接着command4位，后面是数据
 * @create: 2022-04-28 10:07
 **/
public class MessageEncoder extends MessageToByteEncoder {

    private Class genericClass;

    public MessageEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if(msg instanceof MessagePack){
//            Message msgBody = (Message) msg;
//            Integer command = msgBody.getMessageHeader().getCommand();
//            MessagePack body = msgBody.getMessagePack();
//
//            String s = JSONObject.toJSONString(body);
//            byte[] bytes = s.getBytes();
//            out.writeInt(bytes.length);
//            out.writeInt(command);
//            out.writeBytes(bytes);
            MessagePack msgBody = (MessagePack) msg;
            String s = JSONObject.toJSONString(msgBody);
            ByteBuf byteBuf = Unpooled.directBuffer(8+s.length());
            byte[] bytes = s.getBytes();
            out.writeInt(bytes.length);
            out.writeInt(msgBody.getCommand());
            out.writeBytes(bytes);
        }
    }

    /**
     * 将Object对象转byte数组
     * @param obj byte数组的object对象
     * @return
     */
    public static byte[] toByteArray(MessagePack obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray ();
            oos.close();
            bos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytes;
    }
}
