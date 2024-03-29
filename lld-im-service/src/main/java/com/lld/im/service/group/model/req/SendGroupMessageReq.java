package com.lld.im.service.group.model.req;

import com.lld.im.common.enums.SyncFromEnum;
import com.lld.im.common.model.RequestBase;
import com.lld.im.common.model.msg.OfflinePushInfo;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-10-12 09:01
 **/
@Data
public class SendGroupMessageReq extends RequestBase {

    //客户端传的messageId
    private String messageId;

    private String fromId;

    private String groupId;

    private int messageRandom;

    private long messageTime;

    private String messageBody;
    /**
     * 这个字段缺省或者为 0 表示需要计数，为 1 表示本条消息不需要计数，即右上角图标数字不增加
     */
    private int badgeMode;

    private Long messageLifeTime;

    private int syncFromId = SyncFromEnum.BOTH.getCode();

    private Integer appId;

    private OfflinePushInfo offlinePushInfo;

}
