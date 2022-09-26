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

    private Long messageSequence;

    //消息接收确认时使用
    private Boolean serverSend;
//    private String conversationId;

    public ChatMessageAck(String messageId, long messageSequence) {
        this.messageId = messageId;
        this.messageSequence = messageSequence;
    }
}
