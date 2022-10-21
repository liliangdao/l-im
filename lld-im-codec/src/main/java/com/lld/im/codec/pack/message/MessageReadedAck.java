package com.lld.im.codec.pack.message;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Chackylee
 * @description: 客户端发给服务端，消息已读ack报文
 * @create: 2022-08-16 14:27
 **/
@Data
@NoArgsConstructor
public class MessageReadedAck {

    private String fromId;

    private String toId;

    private int conversationType;

    private long messageSequence;
}
