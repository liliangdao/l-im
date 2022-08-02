package com.lld.im.tcp.reciver.process;

import com.lld.im.codec.proto.Message;
import com.lld.im.codec.proto.MessageHeader;
import com.lld.im.codec.proto.MessagePack;
import com.lld.im.tcp.utils.SessionSocketHolder;
import com.rabbitmq.client.Channel;
import io.netty.channel.AbstractChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;

public abstract class MessageProcess {

    public void process(MessagePack pack,Channel mqChannel) throws IOException {
        NioSocketChannel channel = SessionSocketHolder
                .get(pack.getAppId(), pack.getToId(), pack.getClientType(), pack.getImei());
        if(channel != null){
            channel.writeAndFlush(pack);
        }
        doProcess(pack,channel,mqChannel);
    }
    protected abstract void doProcess(MessagePack pack, AbstractChannel channel, Channel mqChannel) throws IOException;

}
