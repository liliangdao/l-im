package com.lld.im.tcp.redis;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.config.BootstrapConfig;
import com.lld.im.codec.proto.Message;
import com.lld.im.codec.proto.MessagePack;
import com.lld.im.codec.proto.MessageHeader;
import com.lld.im.common.ClientType;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.DeviceMultiLoginEnum;
import com.lld.im.common.enums.MessageCommand;
import com.lld.im.common.model.UserClientDto;
import com.lld.im.tcp.reciver.UserLoginMessageListener;
import com.lld.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;

import java.util.List;

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
