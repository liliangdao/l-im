package com.lld.im.common.enums;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 09:51
 **/
public enum FriendShipStatusEnum  {

    FRIEND_STATUS_NO_FRIEND(0,"未添加"),

    FRIEND_STATUS_NORMAL(1,"正常"),

    FRIEND_STATUS_DELETED(2,"删除"),

    BLACK_STATUS_NORMAL(1,"正常"),

    BLACK_STATUS_BLACKED(2,"拉黑"),
    ;

    private int status;
    private String desc;

    FriendShipStatusEnum(int status, String desc){
        this.status = status;
        this.desc = desc;
    }

    public int getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
