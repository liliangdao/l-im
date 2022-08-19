package com.lld.im.codec.pack;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Chackylee
 * @description: 单聊消息ack
 * @create: 2022-08-16 14:24
 **/
@Data
@NoArgsConstructor
public class ChatMessageAck {
    private String messageId;

    private long messageSequence;
//    private String conversationId;

    public ChatMessageAck(String messageId, long messageSequence,int appId) {
        this.messageId = messageId;
        this.messageSequence = messageSequence;
    }
}
