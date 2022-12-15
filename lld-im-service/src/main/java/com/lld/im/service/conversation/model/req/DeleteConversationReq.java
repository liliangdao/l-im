package com.lld.im.service.conversation.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-12-15 09:14
 **/
@Data
public class DeleteConversationReq extends RequestBase {

    private String conversationId;

    private Integer conversationType;

    private String fromId;

}
