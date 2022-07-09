package com.lld.im.service.friendship.dao;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 14:32
 **/
@Data
@TableName("im_friendship_request")
public class ImFriendShipRequestEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer appId;

    private String fromId;

    private String toId;
    /** 备注*/
//    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String remark;

    //是否已读 1已读
    private Integer readStatus;

    /** 好友来源*/
//    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String addSource;

//    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String addWording;

//    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long createTime;




}
