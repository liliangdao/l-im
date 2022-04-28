package com.lld.im.proto;

import com.lld.im.enums.SyncFromEnum;
import lombok.Data;

import java.io.Serializable;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-04-28 10:12
 **/
@Data
public class MsgBody implements Serializable {

    private String msgId;

    private String fromId;

    private String toId;

    private int msgRandom;

    private long msgTime;

    private long msgSequence;

    private Object msgBody;
    /**
     * 这个字段缺省或者为 0 表示需要计数，为 1 表示本条消息不需要计数，即右上角图标数字不增加
     */
    private int badgeMode;

    private Long msgLifeTime;


    private int syncFromId = SyncFromEnum.BOTH.getCode();

    private Integer appId;

    private int clientType;
}
