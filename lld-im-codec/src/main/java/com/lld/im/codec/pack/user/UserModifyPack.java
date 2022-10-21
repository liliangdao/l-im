package com.lld.im.codec.pack.user;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 服务端发给客户端，用户信息变更通知报文
 * @create: 2022-08-02 10:26
 **/
@Data
public class UserModifyPack  {

    // 用户id
    private String userId;

    // 用户名称
    private String nickName;

    private String password;

    // 头像
    private String photo;

    // 性别
    private String userSex;

    // 个性签名
    private String selfSignature;

    // 加好友验证类型（Friend_AllowType） 1需要验证
    private Integer friendAllowType;
}
