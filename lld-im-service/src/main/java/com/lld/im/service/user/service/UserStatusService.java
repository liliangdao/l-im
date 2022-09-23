package com.lld.im.service.user.service;

import com.lld.im.service.user.model.UserOnlineStatusContent;
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

    private void processUserLoginNotify(UserOnlineStatusContent content){

    }

}
