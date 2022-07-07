package com.lld.im.tcp.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.UserPipelineConnectState;
import com.lld.im.common.model.UserSession;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since JDK 1.8
 */
//TODO 改为非spring的redis
public class SessionSocketHolder {
    private static final Map<String, NioSocketChannel> CHANNEL_MAP = new ConcurrentHashMap<>(16);
    /**
     * Save the relationship between the userId and the channel.
     *
     * @param id
     * @param socketChannel
     */
    public static void put(Integer appId ,String id,Integer client,String imel, NioSocketChannel socketChannel) {
        CHANNEL_MAP.put(appId + ":" +id+":"+client + ":" + imel, socketChannel);
    }

    public static NioSocketChannel get(Integer appId ,String id,Integer client,String imel) {
        return CHANNEL_MAP.get(appId + ":" +id+":"+client + ":" + imel);
    }

    public static List<NioSocketChannel> get(Integer appId , String id) {

        Set<String> keys = CHANNEL_MAP.keySet();
        List<NioSocketChannel> channels = new ArrayList<>();

        keys.forEach(key ->{

            if(key.startsWith(appId+":"+id)){
                channels.add(CHANNEL_MAP.get(key));
            }
        });

        return channels;
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
    public static void offlineUserSession(NioSocketChannel nioSocketChannel) {

//        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
//        String clientInfo = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientImei)).get();
//        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
//
//        StringRedisTemplate stringRedisTemplate = SpringBeanFactory.getBean(StringRedisTemplate.class);
//        String sessionStr = (String) stringRedisTemplate.opsForHash().get(appId + ":" + Constants.RedisConstants.UserSessionConstants + ":" + userId,
//                clientInfo);
//        if (!StringUtils.isEmpty(sessionStr)) {
//            UserSession UserSession = JSONObject.parseObject(sessionStr, UserSession.class);
//            UserSession.setConnectState(UserPipelineConnectState.OFFLINE.getCommand());
//            stringRedisTemplate.opsForHash().put(appId + ":" + Constants.RedisConstants.UserSessionConstants + ":" + userId,clientInfo,
//                    JSON.toJSONString(UserSession));
//        }
//        remove(nioSocketChannel);
//        nioSocketChannel.close();
    }

    /**
     * @description 删除用户session，通常用于用户手动下线
     * @author chackylee
     * @date 2022/5/7 11:43
     * @param [nioSocketChannel]
     * @return void
    */
    public static void removeUserSession(NioSocketChannel nioSocketChannel) {
//        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
//        String clientInfo = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientImei)).get();
//        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
//        StringRedisTemplate stringRedisTemplate = SpringBeanFactory.getBean(StringRedisTemplate.class);
//        stringRedisTemplate.opsForHash().delete(appId+":"+ Constants.RedisConstants.UserSessionConstants+":"+userId,clientInfo);
//        remove(nioSocketChannel);
//        nioSocketChannel.close();
    }




}
