package com.lld.im.common.enums;

public enum CheckFriendTypeEnum {

    /**
     * 1 单方校验；2双方校验。
     */
    SINGLE(1),

    BOTH(2),
    ;

    private int type;

    CheckFriendTypeEnum(int type){
        this.type=type;
    }

    public int getType() {
        return type;
    }
}
