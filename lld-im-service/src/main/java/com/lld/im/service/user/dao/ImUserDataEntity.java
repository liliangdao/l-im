package com.lld.im.service.user.dao;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author: Chackylee
 * @description: 数据库用户数据实体类
 * @create: 2022-05-11 14:12
 **/

@Data
@TableName("im_user_data")
public class ImUserDataEntity {

    // 用户id
    private String userId;

    // 用户名称
    private String nickName;

    private String password;

    // 头像
    private String photo;

    // 性别
    private Integer userSex;

    // 个性签名
    private String selfSignature;

    // 加好友验证类型（Friend_AllowType） 1需要验证
    private Integer friendAllowType;

    // 管理员禁止用户添加加好友：0 未禁用 1 已禁用
    private Integer disableAddFriend;

    // 禁用标识(0 未禁用 1 已禁用)
    private Integer forbiddenFlag;

    // 禁言标识
    private Integer silentFlag;
    /**
     * 用户类型 1普通用户 2客服 3机器人
     */
    private Integer userType;

    private Integer appId;

    private Integer delFlag;


}
