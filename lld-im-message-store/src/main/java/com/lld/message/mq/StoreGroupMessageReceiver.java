package com.lld.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.constant.Constants;
import com.lld.message.dao.ImMessageBodyEntity;
import com.lld.message.model.DoStroeGroupMessageDto;
import com.lld.message.service.StoreMessageService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: Chackylee
 * @description:
 **/
@Component
public class StoreGroupMessageReceiver {

    private static Logger logger = LoggerFactory.getLogger(StoreGroupMessageReceiver.class);

    @Autowired
    StoreMessageService storeMessageService;

    /**
     * 订阅MQ单聊消息队列--处理
     * vf
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = Constants.RabbitConstants.StoreGroupMessage, durable = "true"),
            exchange = @Exchange(value = Constants.RabbitConstants.StoreGroupMessage, durable = "true")
    ), concurrency = "1")
    @RabbitHandler
    public void onMessage(@Payload Message message,
                              @Headers Map<String, Object> headers,
                              Channel channel) throws Exception {

        long start = System.currentTimeMillis();
        Thread t = Thread.currentThread();
        String msg = new String(message.getBody(), "utf-8");
        logger.info("CHAT MSG FROM QUEUE :::::" + msg);
        //deliveryTag 用于回传 rabbitmq 确认该消息处理成功
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);

        try {
            JSONObject jsonObject = JSON.parseObject(msg);
            DoStroeGroupMessageDto doStroeP2PMessageDto = jsonObject.toJavaObject(DoStroeGroupMessageDto.class);
            doStroeP2PMessageDto.setImMessageBodyEntity(jsonObject.getObject("imMessageBody", ImMessageBodyEntity.class));
            storeMessageService.doStoreGroupMessage(doStroeP2PMessageDto);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            logger.error("处理消息出现异常：{}", e.getMessage());
            logger.error("RMQ_CHAT_TRAN_ERROR", e);
            logger.error("NACK_MSG:{}", msg);
            //第一个false 表示不批量拒绝，第二个false表示不重回队列
            channel.basicNack(deliveryTag, false, false);
        } finally {
            long end = System.currentTimeMillis();
            logger.debug("channel {} basic-Ack ,it costs {} ms,threadName = {},threadId={}", channel, end - start, t.getName(), t.getId());
        }

    }


}
