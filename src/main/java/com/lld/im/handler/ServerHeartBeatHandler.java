package com.lld.im.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lld.im.constant.Constants;
import com.lld.im.model.AccountSession;
import com.lld.im.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author: Chackylee
 * @description: 真正处理心跳超时的类
 * @create: 2022-05-06 09:22
 **/
@Component
public class ServerHeartBeatHandler {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * @description 处理用户心跳超时,设置session的connectState为离线
     * @author chackylee
     * @date 2022/5/6 9:23
     * @param [ctx]
     * @return void
    */
    public void process(ChannelHandlerContext ctx){
//        String longId = ctx.channel().id().asLongText();
//        System.out.println(longId + " 心跳超时");
        SessionSocketHolder.offlineAccountSession((NioSocketChannel) ctx.channel());
    }

}
