package com.lld.im.common.model.msg.dto;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 **/
@Data
public class ImMessageBody {

    private Integer appId;
    /** messageBodyId*/
    private Long messageKey;

    /** messageBody*/
    private String messageBody;

    private String securityKey;

    private Long messageTime;

    private Long createTime;

    private String extra;

    private Integer delFlag;

}
