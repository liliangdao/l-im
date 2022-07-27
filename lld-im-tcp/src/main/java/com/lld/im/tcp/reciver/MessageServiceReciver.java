package com.lld.im.tcp.reciver;

import com.lld.im.codec.WebSocketMessageEncoder;
import com.lld.im.tcp.utils.MqFactoryUtils;
import com.rabbitmq.client.Connection;
import com.sun.org.apache.bcel.internal.ExceptionConst;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Chackylee
 * @description: 接收消息服务投递过来的mq消息
 * @create: 2022-07-27 10:34
 **/
public class MessageServiceReciver {

    private static Logger log = LoggerFactory.getLogger(WebSocketMessageEncoder.class);

    private String brokerId;

    public MessageServiceReciver(String brokerId) {
        this.brokerId = brokerId;
    }

    public void startReciverMessage() {
        try {
            Connection connection = MqFactoryUtils.getConnection();
        }catch (Exception e){

        }

    }

}
