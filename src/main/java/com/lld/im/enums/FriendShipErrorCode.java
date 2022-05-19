package com.lld.im.enums;

import com.lld.im.exception.ApplicationExceptionEnum;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 09:51
 **/
public enum FriendShipErrorCode implements ApplicationExceptionEnum {

    REPEAT_TO_ADD(30000,"好友重复添加"),

    FRIEND_ADD_ERROR(30001,"好友添加失败"),

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
