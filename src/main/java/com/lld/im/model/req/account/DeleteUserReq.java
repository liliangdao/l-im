package com.lld.im.model.req.account;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-17 16:56
 **/
@Data
public class DeleteUserReq {

    @NotEmpty(message = "用户id不能为空")
    private List<String> userId;

    private Integer appId;

}
