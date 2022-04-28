package com.lld.im.enums;

public enum SyncFromEnum {

    /**
     *  发送给to方并同步给from方
     */
    BOTH(1),

    /**
     * 消息不同步至 fromId
     */
    SEND_ONLY(2),


    /**
     * 消息只发送给fromId ,不发送给 toId
     */
    FROM_ONLY(3);

    private int code;

    SyncFromEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public boolean getBoolValue() {
        return code == 1;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
