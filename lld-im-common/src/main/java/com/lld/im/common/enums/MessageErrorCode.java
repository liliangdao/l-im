package com.lld.im.common.enums;

import com.lld.im.common.exception.ApplicationExceptionEnum;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-11 14:33
 **/
public enum MessageErrorCode implements ApplicationExceptionEnum {


    FROMER_IS_NOT_EXIST(50000,"发送方不存在"),
    TO_IS_NOT_EXIST(50001,"接收方不存在"),
    FROMER_IS_MUTE(50002,"发送方被禁言"),
    FROMER_IS_FORBIBBEN(50002,"发送方被禁用"),

    MESSAGEBODY_IS_NOT_EXIST(50003,"消息体不存在"),

    MESSAGE_RECALL_TIME_OUT(50004,"消息已超过可撤回时间"),

    MESSAGE_IS_RECALLED(50005,"消息已被撤回"),

            ;

    private int code;
    private String error;

    MessageErrorCode(int code, String error){
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
