package com.lld.im.common.model.msg;

import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-22 15:58
 **/
@Data
public class GroupChatMessageContent extends MessageContent {

    private String groupId;
    private OfflinePushInfo offlinePushInfo;

    private List<String> members;

}
