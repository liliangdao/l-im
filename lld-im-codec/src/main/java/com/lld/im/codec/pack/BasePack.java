package com.lld.im.codec.pack;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 公共请求报文，只有前端发过来的报文需要继承这个类，服务端发送的报文不需要继承
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
