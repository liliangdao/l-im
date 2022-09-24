package com.lld.im.service.user.service;

import com.lld.im.common.constant.Constants;
import com.lld.im.service.user.model.UserOnlineStatusChangeContent;
import com.lld.im.service.user.model.UserOnlineStatusSubscribeContent;
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
        Set<Object> keys = stringRedisTemplate.opsForHash().keys(userKey);

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
