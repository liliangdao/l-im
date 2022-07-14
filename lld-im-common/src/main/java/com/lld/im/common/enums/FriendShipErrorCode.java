package com.lld.im.common.enums;

import com.lld.im.common.exception.ApplicationExceptionEnum;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 09:51
 **/
public enum FriendShipErrorCode implements ApplicationExceptionEnum {

    REPEAT_TO_ADD(30000,"好友重复添加"),

    FRIEND_ADD_ERROR(30001,"好友添加失败"),

    ADD_FRIEND_NEED_VERIFY(30002,"对方开启了好友验证，已发送好友申请给对方"),

    ADD_FRIEND_REQUEST_ERROR(30003,"好友验证添加失败"),

    FRIEND_IS_DELETED(30004,"好友已被删除"),

    ;

    private int code;
    private String error;

    FriendShipErrorCode(int code, String error){
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
