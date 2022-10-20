package com.lld.im.tcp.redis;

import java.util.HashMap;
import java.util.Map;

/**
 * 客戶端公長類
 *
 */
public class ClientFactory {

    public static Map<String, ClientStrategy> strategyMap = new HashMap<>();

    static {
        strategyMap.put("single", new SingleClientStrategy());
        strategyMap.put("sentinel", new SentinelClientStrategy());
        strategyMap.put("cluster", new ClusterClientStrategy());
    }

    /**
     * 获取客户端策略
     *
     * @param key
     * @return {@link ClientStrategy}
     */
    public static ClientStrategy getClientStrategy(String key) {
        return strategyMap.get(key);
    }
}
