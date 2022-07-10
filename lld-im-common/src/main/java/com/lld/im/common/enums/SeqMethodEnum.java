package com.lld.im.common.enums;

public enum SeqMethodEnum {

    /**
     * snowflake
     */
    SNOWFLAKE(1,"com.lld.im.service.service.seq.SnowflakeSeq"),

    /**
     * redis
     */
    REDIS(2,"com.lld.im.service.service.seq.RedisSeq"),

    ;


    private int code;
    private String clazz;

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     * @param ordinal
     * @return
     */
    public static SeqMethodEnum getHandler(int ordinal) {
        for (int i = 0; i < SeqMethodEnum.values().length; i++) {
            if (SeqMethodEnum.values()[i].getCode() == ordinal) {
                return SeqMethodEnum.values()[i];
            }
        }
        return null;
    }

    SeqMethodEnum(int code, String clazz){
        this.code=code;
        this.clazz=clazz;
    }

    public String getClazz() {
        return clazz;
    }

    public int getCode() {
        return code;
    }
}
