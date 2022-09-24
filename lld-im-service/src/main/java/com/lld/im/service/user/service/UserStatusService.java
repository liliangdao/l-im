package com.lld.im.service.user.service;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.constant.Constants;
import com.lld.im.service.message.mq.ChatOperateReceiver;
import com.lld.im.service.user.model.UserOnlineStatusChangeContent;
import com.lld.im.service.user.model.UserOnlineStatusSubscribeContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * @description:
 * @author: lld
 * @createDate: 2022-09-23
 * @version: 1.0
 */
@Service
public class UserStatusService {

    private static Logger logger = LoggerFactory.getLogger(UserStatusService.class);

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * @description: 用户上线通知，通知给订阅了这个用户的人
     * @param
     * @return void
     * @author lld 
     * @since 2022/9/24
     */
    public void processUserLoginNotify(UserOnlineStatusChangeContent content){

        String userKey = content.getAppId()
                + ":" + Constants.RedisConstants.subscribe +":" +content.getUserId();
//        stringRedisTemplate.opsForHash().m
        Set<Object> keys = stringRedisTemplate.opsForHash().keys(userKey);
        for (Object key:
             keys) {
            String filed = (String) key;
            Long expire = (Long) stringRedisTemplate.opsForHash().get(userKey, filed);
            if(expire > 0 && expire > System.currentTimeMillis()){
                //往mq发送消息

            }
        }

        logger.info(JSONObject.toJSONString(keys));
    }

    /**
     * @description: 处理用户订阅某个用户
     * @param
     * @return void
     * @author lld
     * @since 2022/9/24
     */
    public void processUserSubscribeNotify(UserOnlineStatusSubscribeContent content){
        String userKey = content.getAppId()
                + ":" + Constants.RedisConstants.subscribe +":" +content.getBeSubUserId();
        Long subExpireTime = 0L;
        if(content != null && content.getSubTime() > 0){
            subExpireTime = System.currentTimeMillis() + content.getSubTime();
        }
        stringRedisTemplate.opsForHash().put(userKey,content.getUserId(),subExpireTime.toString());
    }

}
