package com.lld.im.codec;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.enums.MsgCommand;
import com.lld.im.proto.Msg;
import com.lld.im.proto.MsgBody;
import com.lld.im.proto.MsgHeader;
import com.lld.im.utils.ByteBufToMsgUtils;
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
 **/
public class MessageDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List out) throws Exception {

        /** 如果可读的数据小于8表示没有数据，4位表示数据长度4位表示command，不做处理*/
        if (in.readableBytes() < 8) {
            return;
        }

        out.add(ByteBufToMsgUtils.transition(in));
    }

}
