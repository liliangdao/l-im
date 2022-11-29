package com.lld.message.dao;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-22 15:38
 **/
@Data
@TableName("im_message_body")
public class ImMessageBodyEntity {

    private Integer appId;
    /** messageBodyId*/
    @TableId
    private Long messageKey;

    /** messageBody*/
    private String messageBody;

    private String securityKey;

    private Long messageTime;

    private Long createTime;

    private String extra;

    private Integer delFlag;

}
