package com.lld.im.service.friendship.model.req;


import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 09:18
 **/
@Data
public class DeleteFriendReq {

    @NotBlank(message = "用户id不能为空")
    private String fromId;

    @NotBlank(message = "好友id不能为空")
    private String toId;

    private Integer appId;
}
