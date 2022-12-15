package com.lld.im.service.conversation.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-12-15 09:14
 **/
@Data
public class UpdateConversationReq extends RequestBase {

    private String conversationId;

    private Integer isMute;

    private Integer isTop;

    private String fromId;
}
