package com.lld.im.tcp.reciver.process;

import com.lld.im.codec.proto.MessagePack;
import com.lld.im.tcp.utils.SessionSocketHolder;
import com.rabbitmq.client.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
public abstract class MessageProcess {

    public abstract void process(MessagePack pack, Channel mqChannel) throws IOException;

}
