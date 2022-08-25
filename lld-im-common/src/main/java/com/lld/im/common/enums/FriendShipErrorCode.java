package com.lld.im.common.enums;

import com.lld.im.common.exception.ApplicationExceptionEnum;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 09:51
 **/
public enum FriendShipErrorCode implements ApplicationExceptionEnum {

    REPEAT_TO_ADD(30000,"好友重复添加"),

    FRIEND_ADD_ERROR(30001,"好友添加失败"),

    ADD_FRIEND_NEED_VERIFY(30002,"对方开启了好友验证，已发送好友申请给对方"),

    ADD_FRIEND_REQUEST_ERROR(30003,"好友验证添加失败"),

    FRIEND_IS_DELETED(30004,"好友已被删除"),

    TARGET_IS_DELETE_YOU(30005,"你已被好友删除"),

    FRIEND_IS_BLACK(30006,"好友已被拉黑"),

    TARGET_IS_BLACK_YOU(30007,"好友已被拉黑"),

    REPEATSHIP_IS_NOT_EXIST(30008,"关系链记录不存在"),

    UPDATE_FRIEND_SHIP_TO_LONG(30009,"更新好友数量过长"),

    NOT_APPROVER_OTHER_MAN_REQUEST(30010,"无法审批其他人的好友请求"),

    FRIEND_REQUEST_IS_NOT_EXIST(30011,"好友申请不存在"),

    FRIEND_SHIP_GROUP_IS_EXIST(30012,"好友分组已存在"),

    FRIEND_SHIP_GROUP_IS_NOT_EXIST(30013,"好友分组不存在"),

    ;

    private int code;
    private String error;

    FriendShipErrorCode(int code, String error){
        this.code = code;
        this.error = error;
    }
    public int getCode() {
        return this.code;
    }

    public String getError() {
        return this.error;
    }

}
