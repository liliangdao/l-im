package com.lld.im.tcp.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.pack.user.UserStatusChangeNotifyPack;
import com.lld.im.codec.proto.MessageHeader;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.UserPipelineConnectState;
import com.lld.im.common.enums.command.UserEventCommand;
import com.lld.im.common.model.ChannelInfo;
import com.lld.im.common.model.UserSession;
import com.lld.im.tcp.publish.MqMessageProducer;
import com.lld.im.tcp.redis.RedisManager;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since JDK 1.8
 */
public class SessionSocketHolder {
    private static final Map<ChannelInfo, NioSocketChannel>
            CHANNELS = new ConcurrentHashMap<>(16);
    /**
     * Save the relationship between the userId and the channel.
     *
     * @param id
     * @param socketChannel
     */
    public static void put(Integer appId ,String id,Integer client,String imel, NioSocketChannel socketChannel) {
        ChannelInfo channelInfo = new ChannelInfo(id, appId, client, imel);
        CHANNELS.put(channelInfo,socketChannel);
    }

    public static NioSocketChannel get(Integer appId ,String id,Integer client,String imel) {
        ChannelInfo channelInfo = new ChannelInfo(id, appId, client, imel);
        return CHANNELS.get(channelInfo);
    }

    public static List<NioSocketChannel> get(Integer appId , String id) {

        Set<ChannelInfo> channelInfos = CHANNELS.keySet();

        List<NioSocketChannel> channels = new ArrayList<>();

        channelInfos.forEach(channel ->{
                if(channel.getAppId().equals(appId) && id.equals(channel.getUserId())){
                channels.add(CHANNELS.get(channel));
            }
        });

        return channels;
    }

    public static void remove(NioSocketChannel nioSocketChannel) {
        CHANNELS.entrySet().stream().filter(entry -> entry.getValue() == nioSocketChannel).forEach(entry -> CHANNELS.remove(entry.getKey()));
    }

    public static void remove(Integer appId ,String id,Integer client,String imel) {
        ChannelInfo channelInfo = new ChannelInfo(id, appId, client, imel);
        CHANNELS.remove(channelInfo);
    }


    /**
     * @description 设置用户离线，通常用于心跳超时,切后台
     * @author chackylee
     * @date 2022/5/7 11:43
     * @param [nioSocketChannel]
     * @return void
    */
    public static void offlineUserSession(NioSocketChannel nioSocketChannel) {

        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        String clientInfo = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientImei)).get();
        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();

        if(StringUtils.isBlank(userId) || StringUtils.isBlank(clientInfo) || appId == null|| clientType == null){
            remove(nioSocketChannel);
            return;
        }

        UserStatusChangeNotifyPack pack = new UserStatusChangeNotifyPack();
        pack.setAppId(appId);
        pack.setUserId(userId);
        pack.setStatus(UserPipelineConnectState.OFFLINE.getCommand());

        MessageHeader header = new MessageHeader();
        header.setAppId(appId);
        header.setImei(imei);
        header.setClientType(clientType);

        //发送在线状态修改信息-》通知用户
        try {
            MqMessageProducer.sendMessageByCommand(pack,header, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY.getCommand());
        }catch (Exception e){
            e.printStackTrace();
        }

        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<String, String> map = redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstants  + userId);
        String sessionStr = map.get(clientInfo);

        if (!StringUtils.isEmpty(sessionStr)) {
            UserSession UserSession = JSONObject.parseObject(sessionStr, UserSession.class);
            UserSession.setConnectState(UserPipelineConnectState.OFFLINE.getCommand());
            map.put(clientInfo,JSON.toJSONString(UserSession));
        }
        remove(appId,userId,clientType,imei);
        nioSocketChannel.close();
    }

    /**
     * @description 删除用户session，通常用于用户手动下线
     * @author chackylee
     * @date 2022/5/7 11:43
     * @param nioSocketChannel
     * @return void
    */
    public static void removeUserSession(NioSocketChannel nioSocketChannel) {
        String userId = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get();
        String clientInfo = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientImei)).get();
        Integer appId = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.AppId)).get();
        String imei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.Imei)).get();
        Integer clientType = (Integer) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientType)).get();

        if(StringUtils.isBlank(userId) || StringUtils.isBlank(clientInfo) || appId == null){
            remove(nioSocketChannel);
            return;
        }

        UserStatusChangeNotifyPack pack = new UserStatusChangeNotifyPack();
        pack.setAppId(appId);
        pack.setUserId(userId);
        String[] split = clientInfo.split(":");
        pack.setStatus(UserPipelineConnectState.OFFLINE.getCommand());

        MessageHeader header = new MessageHeader();
        header.setAppId(appId);
        header.setImei(split[1]);
        header.setClientType(Integer.valueOf(split[0]));

        //发送在线状态修改信息-》通知用户
        try {
            MqMessageProducer.sendMessageByCommand(pack,header, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY.getCommand());
        }catch (Exception e){
            e.printStackTrace();
        }

        RedissonClient redissonClient = RedisManager.getRedissonClient();
        RMap<Object, Object> map = redissonClient.getMap(appId + Constants.RedisConstants.UserSessionConstants + userId);
        map.remove(clientInfo);
        remove(appId,userId,clientType,imei);
        nioSocketChannel.close();
    }

}
