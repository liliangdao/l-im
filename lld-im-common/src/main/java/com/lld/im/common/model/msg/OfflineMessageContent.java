package com.lld.im.common.model.msg;

import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-29 15:53
 **/
@Data
public class OfflineMessageContent {

    private Long messageKey;

    private List<String> members;

    private Integer conversationType;

    private String conversationId;

    private String fromId;

    private String toId;

    private long messageTime;

    private long messageSequence;

    private String messageBody;
    /**
     * 这个字段缺省或者为 0 表示需要计数，为 1 表示本条消息不需要计数，即右上角图标数字不增加
     */
    private int badgeMode;

    private Long messageLifeTime;

    private Integer appId;

    private Integer delFlag;

}
