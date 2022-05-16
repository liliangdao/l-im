package com.lld.im.common.route.algorithm.random;


import com.lld.im.enums.UserErrorCode;
import com.lld.im.exception.ApplicationException;
import com.lld.im.common.route.RouteHandle;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 路由策略， 随机
 *
 * @since JDK 1.8
 */
public class RandomHandle implements RouteHandle {

    @Override
    public String routeServer(List<String> values, String key) {
        int size = values.size();
        if (size == 0) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE) ;
        }
        int offset = ThreadLocalRandom.current().nextInt(size);

        return values.get(offset);
    }
}
