package com.lld.im.codec.pack.message;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-10-21 09:36
 **/
@Data
public class ChatMessagePack {

    //客户端传的messageId
    private String messageId;

    private Long messageKey;

    private String fromId;

    private String toId;

    private int messageRandom;

    private long messageTime;

    private long messageSequence;

    private String messageBody;
    /**
     * 这个字段缺省或者为 0 表示需要计数，为 1 表示本条消息不需要计数，即右上角图标数字不增加
     */
    private int badgeMode;

    private Long messageLifeTime;

    private Integer appId;


}
