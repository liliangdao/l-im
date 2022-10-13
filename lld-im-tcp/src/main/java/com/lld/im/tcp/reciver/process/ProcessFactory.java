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

    private static MessageProcess userEventMessageProcess;
    private static MessageProcess chatMessageProcess;
    private static MessageProcess defatultProcess;

    static {
        userEventMessageProcess = new UserEventMessageProcess();
        chatMessageProcess = new ChatMessageProcess();
        defatultProcess = new MessageProcess() {
            @Override
            protected void doProcess(MessagePack pack, AbstractChannel channel, Channel mqChannel) throws IOException {

            }
        };
    }

    public static MessageProcess getMessageProcess(Integer command){
        if(command.toString().startsWith("4")){
            //4开头表示是用户消息
            return userEventMessageProcess;
        }
        if(command.toString().startsWith("1")){
            //2开头表示是用户消息
            return chatMessageProcess;
        }

        if(command.toString().startsWith("4")){
            //2开头表示是用户消息
            return chatMessageProcess;
        }

        return defatultProcess;
    }

}
