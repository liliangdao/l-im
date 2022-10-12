package com.lld.im.service.message.model.resp;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-10-12 09:01
 **/
@Data
public class SendMessageResp {

    private Long messageKey;

    private String messageId;

    private long messageTime;


}
