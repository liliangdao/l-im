package com.lld.im.service.user.model.req;

import lombok.Data;

import javax.validation.constraints.NotNull;


/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-16 10:38
 **/
@Data
public class LoginReq {

    @NotNull(message = "用户id不能位空")
    private String userId;

    private Integer clientType;

}
