package com.lld.im.codec.pack.message;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 单聊消息分发报文
 * @create: 2022-10-12 10:15
 **/
@Data
public class P2PMessagePack {

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
