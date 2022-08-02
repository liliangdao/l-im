package com.lld.im.codec.proto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Chackylee
 * @description: 消息实体
 * @create: 2022-04-25 09:02
 **/
@Data
@NoArgsConstructor
public class Message {

    private MessageHeader messageHeader;

    private MessagePack messagePack;

    public static Message createMesssage(MessagePack pack){
        Message message = new Message();
        MessageHeader header = new MessageHeader();
        header.setCommand(pack.getCommand());
        message.setMessageHeader(header);
        message.setMessagePack(pack);
        return message;
    }

    public Message(MessagePack pack) {
        MessageHeader header = new MessageHeader();
        header.setCommand(pack.getCommand());
        this.messageHeader = header;
        this.messagePack = pack;
    }
}
