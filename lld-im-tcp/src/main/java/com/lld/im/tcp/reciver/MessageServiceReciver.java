package com.lld.im.tcp.reciver;

/**
 * @author: Chackylee
 * @description: 接收消息服务投递过来的mq消息
 * @create: 2022-07-27 10:34
 **/
public class MessageServiceReciver {

    private String brokerId;

    public MessageServiceReciver(String brokerId) {
        this.brokerId = brokerId;
    }

}
