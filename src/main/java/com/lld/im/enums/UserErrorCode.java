package com.lld.im.enums;

import com.lld.im.exception.ApplicationExceptionEnum;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-11 14:33
 **/
public enum UserErrorCode implements ApplicationExceptionEnum {


    IMPORT_SIZE_BEYOND(20000,"导入數量超出上限"),

            ;

    private int code;
    private String error;

    UserErrorCode(int code, String error){
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