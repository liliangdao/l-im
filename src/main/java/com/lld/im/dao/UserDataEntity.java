package com.lld.im.dao;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 数据库用户数据实体类
 * @create: 2022-05-11 14:12
 **/

@Data
public class UserDataEntity {

    // 用户id
    private String userId;

    // 用户名称
    private String nickName;

    // 头像
    private String photo;

    // 性别
    private String userSex;

    // 个性签名
    private String selfSignature;

    // 加好友验证类型（Friend_AllowType）
    private Integer friendAllowType;

    // 管理员禁止用户添加加好友：0 未禁用 1 已禁用
    private Integer disableAddFriend;

    // 禁用标识(0 未禁用 1 已禁用)
    private Integer forbiddenFlag;
    /**
     * 用户类型 1普通用户 2客服 3机器人
     */
    private Integer userType;

    private Integer appId;

    private Integer delFlag;


}
