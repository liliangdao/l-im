package com.lld.im.enums;

import com.lld.im.exception.ApplicationExceptionEnum;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 09:51
 **/
public enum FriendShipEnum  {

    FRIEND_STATUS_NO_FRIEND(0,"未添加"),

    FRIEND_STATUS_NORMAL(1,"正常"),

    FRIEND_STATUS_DELETED(2,"删除"),

    BLACK_STATUS_NORMAL(1,"正常"),

    BLACK_STATUS_BLACKED(2,"拉黑"),
    ;

    public int code;
    public String desc;

    FriendShipEnum(int code, String desc){
        this.code = code;
        this.desc = desc;
    }


}
