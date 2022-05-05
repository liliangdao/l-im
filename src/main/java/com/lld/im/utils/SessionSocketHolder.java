package com.lld.im.utils;

import com.lld.im.model.AccountSession;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since JDK 1.8
 */
public class SessionSocketHolder {
    private static final Map<String, NioSocketChannel> CHANNEL_MAP = new ConcurrentHashMap<>(16);
//    private static final Map<Long, String> SESSION_MAP = new ConcurrentHashMap<>(16);

//    public static void saveSession(Long userId, String userName) {
//        SESSION_MAP.put(userId, userName);
//    }

//    public static void removeSession(Long userId) {
//        SESSION_MAP.remove(userId);
//    }

    /**
     * Save the relationship between the userId and the channel.
     *
     * @param id
     * @param socketChannel
     */
    public static void put(String id, NioSocketChannel socketChannel) {
        CHANNEL_MAP.put(id, socketChannel);
    }

    public static NioSocketChannel get(String id) {
        return CHANNEL_MAP.get(id);
    }

    public static Map<String, NioSocketChannel> getRelationShip() {
        return CHANNEL_MAP;
    }

    public static void remove(NioSocketChannel nioSocketChannel) {
        CHANNEL_MAP.entrySet().stream().filter(entry -> entry.getValue() == nioSocketChannel).forEach(entry -> CHANNEL_MAP.remove(entry.getKey()));
    }

//    /**
//     * 获取注册用户信息
//     *
//     * @param nioSocketChannel
//     * @return
//     */
//    public static AccountSession getUserId(NioSocketChannel nioSocketChannel) {
//        for (Map.Entry<String, NioSocketChannel> entry : CHANNEL_MAP.entrySet()) {
//            NioSocketChannel value = entry.getValue();
//            if (nioSocketChannel == value) {
//                String key = entry.getKey();
//                String userName = SESSION_MAP.get(key);
////                AccountSession info = new TIMUserInfo(key, userName);
//                return null;
//            }
//        }
//        return null;
//    }


}
