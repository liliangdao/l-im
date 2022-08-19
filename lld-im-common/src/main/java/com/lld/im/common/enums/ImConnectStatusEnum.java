package com.lld.im.common.enums;

public enum ImConnectStatusEnum {

    /**
     * 管道链接状态,1=在线，2=离线。。
     */
    ONLINE_STATUS(1),

    OFFLINE_STATUS(2),
    ;

    private Integer code;

    ImConnectStatusEnum(Integer code){
        this.code=code;
    }

    public Integer getCode() {
        return code;
    }
}
