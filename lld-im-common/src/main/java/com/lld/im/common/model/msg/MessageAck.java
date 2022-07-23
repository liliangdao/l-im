package com.lld.im.common.model.msg;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/23
 * @version: 1.0
 */
@Data
@NoArgsConstructor
public class MessageAck {

    private String messageId;

    private long messageSequence;
//    private String conversationId;


    public MessageAck(String messageId, long messageSequence) {
        this.messageId = messageId;
        this.messageSequence = messageSequence;
    }
}
