package com.lld.im.tcp.redis;

import com.lld.im.codec.config.BootstrapConfig;
import com.lld.im.common.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;

import java.util.Map;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-08 11:57
 **/
@Slf4j
public class RedisManager {

    public static RedissonClient redissonClient;

    public static void init(BootstrapConfig config) {
        try {
            // 获取客户端策略
            ClientStrategy clientStrategy = ClientFactory.getClientStrategy(config.getLim().getRedis().getMode());
            // 获取redisson客户端
            redissonClient = clientStrategy.getRedissonClient(config.getLim().getRedis());
        } catch (Exception e) {
            log.error("startUp error message", e);
        }
    }

    public static void listenerUserLogin() {
        RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence channel, String msg) {
                System.out.println(msg);
            }
        });
//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        },"listenerUserLogin");
//        thread.start();
    }

}
