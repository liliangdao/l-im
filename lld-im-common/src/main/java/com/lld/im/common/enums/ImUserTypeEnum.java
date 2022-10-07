package com.lld.im.common.enums;

public enum ImUserTypeEnum {

    /**
     * 0 正常；1 删除。
     */
    IM_USER(1),

    APP_ADMIN(100),
    ;

    private int code;

    ImUserTypeEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
