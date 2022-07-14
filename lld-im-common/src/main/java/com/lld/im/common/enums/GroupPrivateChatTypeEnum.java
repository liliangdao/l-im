package com.lld.im.common.enums;

public enum GroupPrivateChatTypeEnum {

    /**
     * 允许私聊
     */
    ALLOW(0),

    /**
     * 禁止私聊
     */
    NOT_ALLOW(1),

    ;

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     * @param ordinal
     * @return
     */
    public static GroupPrivateChatTypeEnum getEnum(Integer ordinal) {

        if(ordinal == null){
            return null;
        }

        for (int i = 0; i < GroupPrivateChatTypeEnum.values().length; i++) {
            if (GroupPrivateChatTypeEnum.values()[i].getCode() == ordinal) {
                return GroupPrivateChatTypeEnum.values()[i];
            }
        }
        return null;
    }

    private int code;

    GroupPrivateChatTypeEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
