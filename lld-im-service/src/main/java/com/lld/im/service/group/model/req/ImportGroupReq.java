package com.lld.im.service.group.model.req;

import com.lld.im.common.model.KeyValuesBase;
import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/9
 * @version: 1.0
 */
@Data
public class ImportGroupReq extends RequestBase {

    private String groupId;
    //群主id
    private String ownerId;

    //群类型 1私有群（类似微信） 2公开群(类似qq）
    private Integer groupType;

    @NotBlank(message = "群名称不能为空")
    private String groupName;

    private Integer mute;// 是否全员禁言，0 不禁言；1 全员禁言。

    private Integer applyJoinType;//加入群权限，0 所有人可以加入；1 群成员可以拉人；2 群管理员或群组可以拉人。

    private String introduction;//群简介

    private String notification;//群公告

    private String photo;//群头像

    private Integer MaxMemberCount;

    private Long createTime;

    private String extra;

}
