//package com.lld.im.codec;
//
//import com.alibaba.fastjson.JSONObject;
//import io.netty.buffer.ByteBuf;
//import io.netty.buffer.Unpooled;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
//import io.netty.util.CharsetUtil;
//
//import java.nio.ByteOrder;
//
///**
// * @author: Chackylee
// * @description:
// * @create: 2022-04-27 10:20
// **/
//public class NettyMessageDecoder extends LengthFieldBasedFrameDecoder {
//    public NettyMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
//        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
//    }
//
//    public NettyMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
//        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
//    }
//
//
//    @Override
//    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
//
//
//
////        int len = in.readableBytes(); //可读的字节数  12
////        System.out.println("len=" + len);
////        //使用for取出各个字节
////        for (int i = 0; i < len; i++) {
////            System.out.println((char) in.getByte(i));
////        }
//
////        System.out.println(new String(array, CharsetUtil.UTF_8));
//
//        ByteBuf frame  = (ByteBuf)super.decode(ctx, in);
//
//        if(frame == null){
//            return null;
//        }
//
//        Object object = new Object();
//        int command = frame.readInt();//
//        byte b = frame.readByte();//消息内容
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("command",command);
//        jsonObject.put("msgBody",b);
//        return jsonObject;
//    }
//
//
//    public static void main(String[] args) throws Exception {
//        NettyMessageDecoder d = new NettyMessageDecoder(Integer.MAX_VALUE,0,4);
//
//
//        ByteBuf byteBuf = Unpooled.buffer(1);
//        byteBuf.writeInt(1046);
//        String string = new String("测试");
//        byteBuf.writeBytes(string.getBytes());
//        d.decode(null,byteBuf);
//
//    }
//
//}
