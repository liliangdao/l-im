package com.lld.im.tcp.redis;

import com.lld.im.codec.config.BootstrapConfig;
import org.redisson.api.RedissonClient;

/**
 * redisson客户策略
 *
 * @author zhaopeng
 */
public interface ClientStrategy {


    /**
     * 获取redisson客户端
     *
     * @param redisConfig
     * @return {@link RedissonClient}
     */
    RedissonClient getRedissonClient(BootstrapConfig.RedisConfig redisConfig);
}
