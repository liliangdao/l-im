package com.lld.im.common;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-11 14:33
 **/
public enum BaseErrorCode {


    SYSTEM_ERROR(90000,"服务器内部错误,请联系管理员"),
    PARAMETER_ERROR(90001,"参数校验错误")

            ;

    private int code;
    private String error;

    BaseErrorCode(int code, String error){
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
