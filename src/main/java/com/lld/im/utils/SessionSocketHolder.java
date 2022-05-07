package com.lld.im.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lld.im.constant.Constants;
import com.lld.im.enums.UserPipelineConnectState;
import com.lld.im.model.AccountSession;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since JDK 1.8
 */
public class SessionSocketHolder {
    private static final Map<String, NioSocketChannel> CHANNEL_MAP = new ConcurrentHashMap<>(16);
    /**
     * Save the relationship between the userId and the channel.
     *
     * @param id
     * @param socketChannel
     */
    public static void put(Integer appId,String id, NioSocketChannel socketChannel) {
        CHANNEL_MAP.put(appId + ":" +id, socketChannel);
    }

    public static NioSocketChannel get(Integer appId ,String id) {
        return CHANNEL_MAP.get(appId + ":" +id);
    }

    public static Map<String, NioSocketChannel> getRelationShip() {
        return CHANNEL_MAP;
    }

    public static void remove(NioSocketChannel nioSocketChannel) {
        CHANNEL_MAP.entrySet().stream().filter(entry -> entry.getValue() == nioSocketChannel).forEach(entry -> CHANNEL_MAP.remove(entry.getKey()));
    }


    /**
     * @description 设置用户离线，通常用于心跳超时
     * @author chackylee
     * @date 2022/5/7 11:43
     * @param [nioSocketChannel]
     * @return void
    */
    public static void offlineAccountSession(NioSocketChannel nioSocketChannel) {

        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        String clientInfo = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientImel)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();

        StringRedisTemplate stringRedisTemplate = SpringBeanFactory.getBean(StringRedisTemplate.class);
        String sessionStr = (String) stringRedisTemplate.opsForHash().get(appId + ":" + Constants.RedisConstants.accountSessionConstants + ":" + userId,
                clientInfo);
        if (!StringUtils.isEmpty(sessionStr)) {
            AccountSession accountSession = JSONObject.parseObject(sessionStr, AccountSession.class);
            accountSession.setConnectState(UserPipelineConnectState.OFFLINE.getCommand());
            stringRedisTemplate.opsForHash().put(appId + ":" + Constants.RedisConstants.accountSessionConstants + ":" + userId,clientInfo,
                    JSON.toJSONString(accountSession));
        }
        remove(nioSocketChannel);
        nioSocketChannel.close();
    }

    /**
     * @description 删除用户session，通常用于用户手动下线
     * @author chackylee
     * @date 2022/5/7 11:43
     * @param [nioSocketChannel]
     * @return void
    */
    public static void removeAccountSession(NioSocketChannel nioSocketChannel) {
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        String clientInfo = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientImel)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        StringRedisTemplate stringRedisTemplate = SpringBeanFactory.getBean(StringRedisTemplate.class);
        stringRedisTemplate.opsForHash().delete(appId+":"+ Constants.RedisConstants.accountSessionConstants+":"+userId,clientInfo);
        remove(nioSocketChannel);
        nioSocketChannel.close();
    }




}
