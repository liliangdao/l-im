package com.lld.im.service.message.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-22 15:38
 **/
@Data
@TableName("im_message_history")
public class ImMessageBodyEntity {

    private Integer appId;
    /** messageBodyId*/
    private String messageKey;

    /** messageBody*/
    private String messageBody;

    private String securityKey;

    private Long messageTime;

    private Long createTime;

}
