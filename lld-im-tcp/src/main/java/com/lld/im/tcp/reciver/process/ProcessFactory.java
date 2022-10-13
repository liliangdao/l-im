package com.lld.im.tcp.reciver.process;

import com.lld.im.codec.proto.MessagePack;
import com.rabbitmq.client.Channel;
import io.netty.channel.AbstractChannel;

import java.io.IOException;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-08-02 10:47
 **/
public class ProcessFactory {

    private static MessageProcess defatultProcess;

    static {
        defatultProcess = new MessageProcess() {
            @Override
            public void process(MessagePack pack, Channel mqChannel) throws IOException {
                super.process(pack, mqChannel);
            }
        };
    }

    public static MessageProcess getMessageProcess(Integer command){
        return defatultProcess;
    }

}
