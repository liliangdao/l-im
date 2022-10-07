package com.lld.im.codec.pack;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-09-30 15:00
 **/
@Data
public class UpdateGroupInfoPack {

    private String groupId;

    private String groupName;

    private Integer mute;// 是否全员禁言，0 不禁言；1 全员禁言。

    private Integer joinType;//加入群权限，0 所有人可以加入；1 群成员可以拉人；2 群管理员或群组可以拉人。

    private String introduction;//群简介

    private String notification;//群公告

    private String photo;//群头像

    private Integer maxMemberCount;//群成员上限
}
