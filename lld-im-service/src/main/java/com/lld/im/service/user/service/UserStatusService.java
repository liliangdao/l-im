package com.lld.im.service.user.service;

import com.lld.im.service.user.model.UserOnlineStatusChangeContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

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
    private void processUserLoginNotify(UserOnlineStatusChangeContent content){
        
    }

}
