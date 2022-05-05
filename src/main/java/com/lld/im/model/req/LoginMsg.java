package com.lld.im.model.req;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-05 09:26
 **/
@Data
public class LoginMsg {

    private String userId;

    private Integer appId;

    /**
     * 端的标识
     */
    private Integer clientType;

    private String imei;

    /**
     * 所用的SDK版本
     */
    private String version;

    /** 用户签名*/
    private String userSign;


}
