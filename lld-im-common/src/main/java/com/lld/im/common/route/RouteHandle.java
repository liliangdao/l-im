package com.lld.im.common.route;

import java.util.List;

/**
 *
 * @since JDK 1.8
 */
public interface RouteHandle {

    /**
     * 再一批服务器里进行路由
     * @param values
     * @param key
     * @return
     */
    String routeServer(List<String> values, String key) ;
}
