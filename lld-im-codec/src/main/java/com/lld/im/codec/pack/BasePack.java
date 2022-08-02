package com.lld.im.codec.pack;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-08-02 10:26
 **/
@Data
public class BasePack {

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
}
