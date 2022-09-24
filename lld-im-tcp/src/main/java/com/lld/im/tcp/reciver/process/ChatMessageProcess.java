package com.lld.im.tcp.reciver.process;

import com.lld.im.codec.proto.MessagePack;
import com.lld.im.common.enums.command.MessageCommand;
import com.rabbitmq.client.Channel;
import io.netty.channel.AbstractChannel;

import java.io.IOException;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-08-02 10:52
 **/
public class ChatMessageProcess extends MessageProcess{

    @Override
    protected void doProcess(MessagePack pack, AbstractChannel channel, Channel mqChannel) throws IOException {
//        if(pack.getCommand() == MessageCommand.MSG_ACK.getCommand()){
//        }
//        else if(pack.getCommand() == MessageCommand.MSG_P2P.getCommand()){
////            if(channel != null){
////                channel.writeAndFlush(pack);
////            }
////            else {
////                mqChannel.basicPublish(Constants.RabbitConstants.Im2MessageService,
////                        "", MessageProperties.PERSISTENT_TEXT_PLAIN,
////                        JSONObject.toJSONString(pack).getBytes());
////            }
//        }
    }
}
