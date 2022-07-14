package com.lld.im.service.group.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import com.lld.im.common.model.KeyValuesBase;
import com.lld.im.service.group.model.req.GroupMemberDto;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/9
 * @version: 1.0
 */
@Data
@TableName("im_group")
public class ImGroupEntity {

    private String groupId;

    private Integer appId;

    //群主id
    private String ownerId;

    //群类型 1私有群（类似微信） 2公开群(类似qq）
    private Integer groupType;

    private String groupName;

    private Integer mute;// 是否全员禁言，0 不禁言；1 全员禁言。

    private Integer joinType;//加入群权限，0 所有人可以加入；1 群成员可以拉人；2 群管理员或群组可以拉人。

    private Integer privateChat; //是否禁止私聊，0 允许群成员发起私聊；1 不允许群成员发起私聊。

    private String introduction;//群简介

    private String notification;//群公告

    private String photo;//群头像

    private Integer maxMemberCount;//群成员上限

    private Integer status;//群状态 0正常 1解散

    private Long sequence;

    private Long createTime;

    private Long updateTime;


}
