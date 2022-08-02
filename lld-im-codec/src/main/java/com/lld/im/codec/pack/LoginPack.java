package com.lld.im.codec.pack;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-05 09:26
 **/
@Data
public class LoginPack extends BasePack{

    private String userId;

    /** 用户签名*/
    private String userSign;


}
