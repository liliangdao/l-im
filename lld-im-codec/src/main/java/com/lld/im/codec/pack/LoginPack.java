package com.lld.im.codec.pack;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-05 09:26
 **/
@Data
public class LoginPack {

    private String userId;

    //客户端状态由业务传递
    private int customStatus;
    //客户端状态字符串
    private String customText;

    //设备名称如 iphone11
    private String customClientName;

}
