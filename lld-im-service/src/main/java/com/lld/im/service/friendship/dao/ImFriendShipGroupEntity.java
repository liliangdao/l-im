package com.lld.im.service.friendship.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.jeffreyning.mybatisplus.anno.AutoMap;
import com.github.jeffreyning.mybatisplus.anno.MppMultiId;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 09:24
 **/

@Data
@TableName("im_friendship_group")
@AutoMap
public class ImFriendShipGroupEntity {

    @TableId(value = "group_id",type = IdType.AUTO)
    private Long groupId;

    private String fromId;

    private Integer appId;

    private String groupName;
    /** 备注*/
    private Long createTime;

    /** 备注*/
    private Long updateTime;

    /** 序列号*/
    private Long sequence;

    private int delFlag;


}
