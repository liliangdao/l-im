package com.lld.im.service.message.model.dto;

import com.lld.im.common.model.msg.ChatMessageContent;
import com.lld.im.common.model.msg.GroupChatMessageContent;
import com.lld.im.service.group.dao.ImGroupMessageHistoryEntity;
import com.lld.im.service.message.dao.ImMessageBodyEntity;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-11-23 16:36
 **/
@Data
public class DoStroeGroupMessageDto {

    private GroupChatMessageContent chatMessageContent;

    private ImMessageBodyEntity imMessageBodyEntity;

}
