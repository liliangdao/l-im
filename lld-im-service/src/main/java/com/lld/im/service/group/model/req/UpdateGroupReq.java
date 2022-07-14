package com.lld.im.service.group.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-14 10:05
 **/
@Data
public class UpdateGroupReq extends RequestBase {

    @NotBlank(message = "群id不能为空")
    private String groupId;

    private String groupName;

    private Integer mute;// 是否全员禁言，0 不禁言；1 全员禁言。

    private Integer joinType;//加入群权限，0 所有人可以加入；1 群成员可以拉人；2 群管理员或群组可以拉人。

    private Integer privateChat; //是否禁止私聊，0 允许群成员发起私聊；1 不允许群成员发起私聊。

    private String introduction;//群简介

    private String notification;//群公告

    private String photo;//群头像

    private Integer maxMemberCount;//群成员上限

}
