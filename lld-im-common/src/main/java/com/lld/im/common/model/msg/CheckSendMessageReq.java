package com.lld.im.common.model.msg;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-12-01 11:30
 **/
@Data
public class CheckSendMessageReq {
    private String fromId;

    private String toId;

    private Integer appId;

    private Integer command;
}
