package com.lld.im.common.enums;

public enum GroupJoinTypeEnum {

    /**
     * 加入群权限，0 所有人可以加入；1 群成员可以拉人；2 群管理员或群组可以拉人
     */
    ALL(0),


    MEMBER(1),

    MANAGER(2),

    ;

    /**
     * 不能用 默认的 enumType b= enumType.values()[i]; 因为本枚举是类形式封装
     * @param ordinal
     * @return
     */
    public static GroupJoinTypeEnum getEnum(Integer ordinal) {

        if(ordinal == null){
            return null;
        }

        for (int i = 0; i < GroupJoinTypeEnum.values().length; i++) {
            if (GroupJoinTypeEnum.values()[i].getCode() == ordinal) {
                return GroupJoinTypeEnum.values()[i];
            }
        }
        return null;
    }

    private int code;

    GroupJoinTypeEnum(int code){
        this.code=code;
    }

    public int getCode() {
        return code;
    }
}
