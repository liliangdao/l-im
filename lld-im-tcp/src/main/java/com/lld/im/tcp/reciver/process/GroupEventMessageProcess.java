package com.lld.im.tcp.reciver.process;

import com.lld.im.codec.proto.MessagePack;
import com.rabbitmq.client.Channel;
import io.netty.channel.AbstractChannel;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-08-02 10:36
 **/
public class GroupEventMessageProcess extends MessageProcess {

    @Override
    public void doProcess(MessagePack pack, AbstractChannel channel, Channel mqChannel) {
    }
}
