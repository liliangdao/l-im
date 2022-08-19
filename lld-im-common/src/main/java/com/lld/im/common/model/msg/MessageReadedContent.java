package com.lld.im.common.model.msg;

import com.lld.im.common.model.ClientInfo;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-08-19 10:35
 **/
@Data
public class MessageReadedContent extends ClientInfo {

    private String fromId;

    private String toId;

    private Integer conversationType;

    private long messageSequence;

}
