package com.lld.im.service.friendship.model.req;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author: Chackylee
 * @description: 添加好友
 * @create: 2022-05-19 09:17
 **/
@Data
public class AddFriendReq {

    @NotBlank(message = "用户id不能为空")
    private String fromId;

    private List<FriendDto> addFriendItems;

    private Integer appId;

}
