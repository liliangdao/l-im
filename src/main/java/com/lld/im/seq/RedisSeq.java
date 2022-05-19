package com.lld.im.seq;

import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 10:30
 **/
public class RedisSeq extends AbstractSeq {

    StringRedisTemplate stringRedisTemplate;

    public void setRedis(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public long doGetSeq(String key) {
        Long increment = stringRedisTemplate.opsForValue().increment(key);
        return increment;
    }
}
