package com.lld.im.common.enums;

public enum GroupMemberRoleEnum {

    /**
     * 普通成员
     */
    ORDINARY(0),

    /**
     * 管理员
     */
    MAMAGER(1),

    /**
     * 群主
     */
    OWNER(2),

    /**
     * 离开
     */
    LEAVE(3);
    ;


    private int code;

    GroupMemberRoleEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
