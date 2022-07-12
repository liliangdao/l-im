package com.lld.im.service.friendship.model.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/9
 * @version: 1.0
 */
@Data
public class SyncFriendShipReq {

    private Integer appId;

    @NotBlank(message = "用户id不能为空")
    private String fromId;

    private long seq;

    private Integer limit;

}