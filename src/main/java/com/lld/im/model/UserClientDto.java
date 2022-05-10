package com.lld.im.model;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-10 10:55
 **/
@Data
public class UserClientDto {

    private Integer appId;

    private String imei;

    private Integer clientType;

    private String userId;

}
