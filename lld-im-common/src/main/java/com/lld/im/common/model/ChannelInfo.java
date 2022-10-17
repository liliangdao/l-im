package com.lld.im.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-10-17 10:10
 **/
@Data
@AllArgsConstructor
public class ChannelInfo {

    private String userId;

    private Integer appId;

    private Integer clientType;

    private String imei;

}
