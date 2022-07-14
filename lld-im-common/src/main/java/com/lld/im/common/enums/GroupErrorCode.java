package com.lld.im.common.enums;

import com.lld.im.common.exception.ApplicationExceptionEnum;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 09:51
 **/
public enum GroupErrorCode implements ApplicationExceptionEnum {

    GROUP_IS_NOT_EXIST(40000,"群不存在"),

    GROUP_IS_EXIST(40001,"群已存在"),

    GROUP_IS_HAVE_OWNER(40002,"群已存在群主"),

    USER_IS_JOINED_GROUP(40003,"该用户已经进入该群"),

    USER_JOIN_GROUP_ERROR(40004,"群成员添加失败"),

    GROUP_MEMBER_IS_BEYOND(40005,"群成员已达到上限"),

    MEMBER_IS_NOT_JOINED_GROUP(40006,"该用户不在群内"),

    THIS_OPERATE_NEED_MANAGER_ROLE(40007,"该操作只允许群主/管理员操作"),

    UPDATE_GROUP_BASE_INFO_ERROR(40008,"更新群信息失败"),

//    ADD_FRIEND_REQUEST_ERROR(30003,"好友验证添加失败"),

    ;

    private int code;
    private String error;

    GroupErrorCode(int code, String error){
        this.code = code;
        this.error = error;
    }
    public int getCode() {
        return this.code;
    }

    public String getError() {
        return this.error;
    }

}
