package com.lld.im.service.friendship.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/8/21
 * @version: 1.0
 */
@Data
public class CheckFriendShipReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    private String fromId;

    //校验类型 1单方校验 2双方校验
    //单方校验 只会检查 From_Account 的好友表中是否有 To_Account，不会检查 To_Account 的好友表中是否有 From_Account
    //双方校验 既会检查 From_Account 的好友表中是否有 To_Account，也会检查 To_Account 的好友表中是否有 From_Account
    private Integer checkType;

    @NotEmpty(message = "toIds不能为空")
    private List<String> toIds;
}
