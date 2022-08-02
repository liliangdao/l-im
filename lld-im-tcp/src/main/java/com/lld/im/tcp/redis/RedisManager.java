package com.lld.im.tcp.redis;

import com.lld.im.codec.config.BootstrapConfig;
import com.lld.im.tcp.reciver.UserLoginMessageListener;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-08 11:57
 **/
@Slf4j
public class RedisManager {

    private static RedissonClient redissonClient;

    private static Integer loginModel;

    public static void init(BootstrapConfig config) {
        try {
            loginModel = config.getLim().getLoginModel();
            // 获取客户端策略
            ClientStrategy clientStrategy = ClientFactory.getClientStrategy(config.getLim().getRedis().getMode());
            // 获取redisson客户端
            redissonClient = clientStrategy.getRedissonClient(config.getLim().getRedis());
//            listenerUserLogin();
            UserLoginMessageListener userLoginMessageListener = new UserLoginMessageListener(loginModel);
            userLoginMessageListener.listenerUserLogin();
        } catch (Exception e) {
            log.error("startUp error message", e);
        }
    }

    public static RedissonClient getRedissonClient(){
        return redissonClient;
    }


}
