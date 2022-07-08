package com.lld.im.tcp.redis;

import com.lld.im.codec.config.BootstrapConfig;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

import java.util.Map;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-08 11:57
 **/
@Slf4j
public class RedisManager {

    RedissonClient redissonClient;

    public void inti(BootstrapConfig.RedisConfig config) {
        try {
            // 获取客户端策略
            ClientStrategy clientStrategy = ClientFactory.getClientStrategy(config.getMode());
            // 获取redisson客户端
            redissonClient = clientStrategy.getRedissonClient(config);
        } catch (Exception e) {
            log.error("startUp error message", e);
        }
    }

}
