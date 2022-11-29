package com.lld.im.service.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.model.msg.ChatMessageContent;
import com.lld.im.common.model.msg.MessageReadedContent;
import com.lld.im.common.model.msg.MessageReciveAckContent;
import com.lld.im.common.model.msg.RecallMessageContent;
import com.lld.im.service.message.model.dto.DoStroeP2PMessageDto;
import com.lld.im.service.message.service.MessageStoreService;
import com.lld.im.service.message.service.MessageSyncService;
import com.lld.im.service.message.service.P2PMessageService;
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
import java.util.Objects;

/**
 * @author: Chackylee
 * @description:
 **/
//@Component
public class StoreMessageReceiver {

    private static Logger logger = LoggerFactory.getLogger(StoreMessageReceiver.class);

    @Autowired
    MessageStoreService messageStoreService;

    /**
     * 订阅MQ单聊消息队列--处理
     *
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = Constants.RabbitConstants.StoreP2PMessage, durable = "true"),
            exchange = @Exchange(value = Constants.RabbitConstants.StoreP2PMessage, durable = "true")
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
            DoStroeP2PMessageDto doStroeP2PMessageDto = jsonObject.toJavaObject(DoStroeP2PMessageDto.class);
            messageStoreService.doStoreP2PMessage(doStroeP2PMessageDto);
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
