package com.lld.im.tcp.handler;

import com.lld.im.common.constant.Constants;
import com.lld.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * @author: Chackylee
 * @description: 真正处理心跳超时的类
 * @create: 2022-05-06 09:22
 **/
@Slf4j
public class ServerHeartBeatHandler {
    /**
     * @param
     * @return void
     * @description 处理用户心跳超时, 设置session的connectState为离线
     * @author chackylee
     * @date 2022/5/6 9:23
     */
    public static void process(Long time, ChannelHandlerContext ctx) {
        Long lastReadTime = (Long) ctx.channel().attr(AttributeKey.valueOf(Constants.ReadTime)).get();
        long now = System.currentTimeMillis();
        if (lastReadTime != null && now - lastReadTime > time) {
            String userId = (String) ctx.channel().attr(AttributeKey.valueOf(Constants.UserId)).get();
            if(StringUtils.isBlank(userId)){
                log.info("用户{}心跳超时，下线处理。",userId);
            }
            SessionSocketHolder.offlineUserSession((NioSocketChannel) ctx.channel());
        }
    }
}
