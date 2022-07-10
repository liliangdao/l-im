package com.lld.im.service.utils;

import com.lld.im.common.constant.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/10
 * @version: 1.0
 */
@Component
public class WriteUserSeq {

    @Autowired
    RedisTemplate redisTemplate;

    public void writeUserSeq(Integer appId,String userId,String seqType,long seq){
        String seqKey = appId+":"+ Constants.RedisConstants.SeqPrefix+":"+userId;
        redisTemplate.opsForHash().put(seqKey,seqType,seq);
    }

}
