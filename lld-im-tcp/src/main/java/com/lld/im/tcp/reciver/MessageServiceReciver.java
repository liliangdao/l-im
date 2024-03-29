package com.lld.im.tcp.reciver;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.proto.MessagePack;
import com.lld.im.common.constant.Constants;
import com.lld.im.tcp.reciver.process.BaseProcess;
import com.lld.im.tcp.reciver.process.MessageProcess;
import com.lld.im.tcp.reciver.process.ProcessFactory;
import com.lld.im.tcp.utils.MqFactory;
import com.rabbitmq.client.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author: Chackylee
 * @description: 接收消息服务投递过来的mq消息
 * @create: 2022-07-27 10:34
 **/
public class MessageServiceReciver {

    private static Logger log = LoggerFactory.getLogger(MessageServiceReciver.class);

    private static String brokerId;

    public static void startReciverMessage() {
        try {
            final Channel channel = MqFactory.getChannel(Constants.RabbitConstants.MessageService2Im);
            channel.queueDeclare(Constants.RabbitConstants.MessageService2Im + brokerId, true, false, false, null);
            channel.queueBind(Constants.RabbitConstants.MessageService2Im + brokerId, Constants.RabbitConstants.MessageService2Im
                    , brokerId);
//            channel.basicQos(1);
            channel.basicConsume(Constants.RabbitConstants.MessageService2Im + brokerId, false , new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String msgStr = new String(body);
                    log.info("收到消息：{}", msgStr);
                    MessagePack messagePack = JSONObject.parseObject(msgStr, MessagePack.class);
                    try {
                        MessageProcess messageProcess = ProcessFactory.getMessageProcess(messagePack.getCommand());
                        messageProcess.process(messagePack,channel);
                        channel.basicAck(envelope.getDeliveryTag() , false);
                    } catch (Exception e){
                        e.printStackTrace();
                        channel.basicNack(envelope.getDeliveryTag(),false,false);
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
            log.error("开启监听im服务消息失败 {}" + e.getMessage());
        }
    }

    public synchronized static void init(String brokerId){
        if(StringUtils.isBlank(MessageServiceReciver.brokerId)){
            MessageServiceReciver.brokerId = brokerId;
            startReciverMessage();
        }
    }

}
