package com.lld.message.model;

import com.lld.im.common.model.msg.GroupChatMessageContent;
import com.lld.message.dao.ImMessageBodyEntity;
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
