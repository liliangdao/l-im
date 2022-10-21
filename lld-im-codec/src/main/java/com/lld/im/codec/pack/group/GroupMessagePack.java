package com.lld.im.codec.pack.group;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 群聊消息分发报文
 * @create: 2022-07-22 15:52
 **/
@Data
public class GroupMessagePack {

    //客户端传的messageId
    private String messageId;

    private String messageKey;

    private String fromId;

    private String groupId;

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
