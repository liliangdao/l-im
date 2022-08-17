package com.lld.im.service.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lld.im.common.ClientType;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ImConnectStatusEnum;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.UserSession;
import com.lld.im.common.model.msg.ChatMessageContent;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/23
 * @version: 1.0
 */
@Service
public class UserSessionUtils {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * @description: 获取用户所有的clientSession
     * @param
     * @return java.util.List<com.lld.im.common.model.UserSession>
     * @author lld
     * @since 2022/7/23
     */
    public List<UserSession> getUserSession(String userId, Integer appId) {

        String userSessionKey = appId + Constants.RedisConstants.UserSessionConstants + userId;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(userSessionKey);

        List<UserSession> list = new ArrayList<>();
        Collection<Object> values = entries.values();
        for (Object o :
                values) {
            String str = (String) o;
            UserSession session = JSON.parseObject(str, new TypeReference<UserSession>() {
            }.getType());
            if(session.getConnectState() == ImConnectStatusEnum.ONLINE_STATUS.getCode()){
                list.add(session);
            }
        }
        return list;
    }

    /**
     * @description: 获取用户指定的client
     * @param
     * @return com.lld.im.common.model.UserSession
     * @author lld
     * @since 2022/7/23
     */
    public UserSession getUserSession(String userId, int clientType, String imei, Integer appId) {
        String userSessionKey = appId + Constants.RedisConstants.UserSessionConstants + userId;
        String hkey = clientType + ":" + imei;
        Object o = stringRedisTemplate.opsForHash().get(userSessionKey, hkey);
        if (o != null) {
            String msg = (String) o;
            UserSession session = JSON.parseObject(msg, new TypeReference<UserSession>() {
            }.getType());
            return session;
        }
        return null;
    }

    public static boolean containMobile(List<ClientInfo> clientInfos) {
        for (ClientInfo clientInfo : clientInfos) {
            int clientType = clientInfo.getClientType();
            if (Objects.equals(ClientType.ANDROID.getCode(), clientType) || Objects.equals(ClientType.IOS.getCode(), clientType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isValid(UserSession userSessionDto) {
        return StringUtils.isNotEmpty(userSessionDto.getUserId()) &&
                userSessionDto.getAppId() != null &&
                userSessionDto.getClientType() != null &&
                userSessionDto.getImei() != null
//                &&
//                userSessionDto.getConnectState() !=null
                ;
    }

}
