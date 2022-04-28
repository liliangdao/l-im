package com.lld.im.codec;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.proto.Msg;
import com.lld.im.proto.MsgBody;
import com.lld.im.proto.MsgHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author: Chackylee
 * @description: 消息编码类
 * @create: 2022-04-28 10:07
 **/
public class MessageEncoder extends MessageToByteEncoder {

    private Class genericClass;

    public MessageEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if(msg instanceof  Msg){
            Msg msgBody = (Msg) msg;
            Integer command = msgBody.getMsgHeader().getCommand();
            MsgBody body = msgBody.getMsgBody();

            String s = JSONObject.toJSONString(body);
            byte[] bytes = s.getBytes();
            out.writeInt(bytes.length);
            out.writeInt(command);
            out.writeBytes(bytes);
        }
    }

    /**
     * 将Object对象转byte数组
     * @param obj byte数组的object对象
     * @return
     */
    public static byte[] toByteArray(MsgBody obj) {
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
