package com.lld.im.service.group.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-22 15:38
 **/
@Data
@TableName("im_group_message_history")
public class ImGroupMessageHistoryEntity {

    private Integer appId;

    private Long messageHistroyId;

    private String fromId;

    private String groupId;

    /** messageBodyId*/
    private String messageKey;
    /** 序列号*/
    private Long sequence;

    private String messageRandom;

    private Long messageTime;

    private Long createTime;

    /** 删除标识*/
    private Integer delFlag;


}
