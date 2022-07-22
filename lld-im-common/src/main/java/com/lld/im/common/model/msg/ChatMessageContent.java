package com.lld.im.common.model.msg;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-22 15:58
 **/
@Data
public class ChatMessageContent extends MessageContent {

    private OfflinePushInfo offlinePushInfo;

}
