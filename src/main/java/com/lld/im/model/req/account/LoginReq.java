package com.lld.im.model.req.account;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-16 10:38
 **/
@Data
public class LoginReq {

    private String userId;

    private Integer clientType;

}
