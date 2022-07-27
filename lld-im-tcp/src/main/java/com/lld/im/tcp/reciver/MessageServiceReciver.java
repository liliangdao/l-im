package com.lld.im.tcp.reciver;

import com.lld.im.codec.WebSocketMessageEncoder;
import com.lld.im.common.constant.Constants;
import com.lld.im.tcp.utils.MqFactoryUtils;
import com.rabbitmq.client.*;
import com.sun.org.apache.bcel.internal.ExceptionConst;
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

    private static Logger log = LoggerFactory.getLogger(WebSocketMessageEncoder.class);

    private static String brokerId;

    public static void startReciverMessage() {
        try {
            Connection connection = MqFactoryUtils.getConnection();
            final Channel channel = connection.createChannel();
            channel.queueDeclare(Constants.RabbitConstants.MessageService2Im, false, false, false, null);
            channel.queueBind(Constants.RabbitConstants.MessageService2Im, Constants.RabbitConstants.MessageService2Im
                    , brokerId);
//            channel.basicQos(1);
            channel.basicConsume(Constants.RabbitConstants.MessageService2Im , false , new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String msgStr = new String(body);
                    System.out.println("收到消息：" + msgStr);
                    try {
                        //TODO 处理消息
                        channel.basicAck(envelope.getDeliveryTag() , false);
                    }catch (Exception e){
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
