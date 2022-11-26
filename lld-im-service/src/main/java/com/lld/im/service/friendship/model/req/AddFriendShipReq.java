package com.lld.im.service.friendship.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author: Chackylee
 * @description: 添加好友，添加黑名单
 **/
@Data
public class AddFriendShipReq extends RequestBase {

    @NotBlank(message = "用户id不能为空")
    private String fromId;

    private List<FriendDto> addItems;
}
