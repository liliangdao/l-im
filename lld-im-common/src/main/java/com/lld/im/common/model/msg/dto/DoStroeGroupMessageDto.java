package com.lld.im.common.model.msg.dto;

import com.lld.im.common.model.msg.GroupChatMessageContent;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-11-23 16:36
 **/
@Data
public class DoStroeGroupMessageDto {

    private GroupChatMessageContent chatMessageContent;

    private ImMessageBody imMessageBody;

}
