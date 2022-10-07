package com.lld.im.service.interceptor;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-10-07 15:57
 **/
public class RequestHolder {

    private final static ThreadLocal<Boolean> requestHolder = new ThreadLocal<>();

    public static void set(Boolean isadmin) {
        requestHolder.set(isadmin);
    }

    public static Boolean get() {
        return requestHolder.get();
    }

    public static void remove() {
        requestHolder.remove();
    }
}
