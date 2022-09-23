package com.lld.im.common.model.msg;

import com.lld.im.common.enums.SyncFromEnum;
import com.lld.im.common.model.ClientInfo;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-22 15:52
 **/
@Data
public class MessageContent extends ClientInfo {

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

    private int syncFromId = SyncFromEnum.BOTH.getCode();

    private Integer appId;

}
