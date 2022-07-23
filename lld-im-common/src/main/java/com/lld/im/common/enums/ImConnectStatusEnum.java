package com.lld.im.common.enums;

public enum ImConnectStatusEnum {

    /**
     * 管道链接状态,1=在线，2=离线。。
     */
    ONLINE_STATUS(1),

    OFFLINE_STATUS(2),
    ;

    private int code;

    ImConnectStatusEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
