package com.lld.im.service.message.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lld.im.codec.pack.MessageReadedPack;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.model.msg.ChatMessageContent;
import com.lld.im.common.model.msg.MessageReadedContent;
import com.lld.im.service.message.service.MessageSyncService;
import com.lld.im.service.message.service.P2PMessageService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;

import java.util.Map;
import java.util.Objects;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-22 15:48
 **/
@Component
public class ChatOperateReceiver {

    private static Logger logger = LoggerFactory.getLogger(ChatOperateReceiver.class);

    @Autowired
    P2PMessageService p2PMessageService;

    @Autowired
    MessageSyncService messageSyncService;

    /**
     * 订阅MQ单聊消息队列--处理
     *
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = Constants.RabbitConstants.Im2MessageService, durable = "true"),
            exchange = @Exchange(value = Constants.RabbitConstants.Im2MessageService, durable = "true")
    ), concurrency = "1")
    @RabbitHandler
    public void onChatMessage(@Payload Message message,
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
            Integer command = jsonObject.getInteger("command");

            //1.接收到新的单聊消息--单聊消息收发  1103
            if (Objects.equals(command, MessageCommand.MSG_P2P.getCommand())) {
                ChatMessageContent messageContent = JSON.parseObject(msg, new TypeReference<ChatMessageContent>() {
                }.getType());
                p2PMessageService.process(messageContent);
            }else if(Objects.equals(command, MessageCommand.MSG_READED.getCommand())){
                //接收方消息已读 --》更新/插入会话表 已读回执是否发送给发送方？
                MessageReadedContent messageContent = JSON.parseObject(msg, new TypeReference<MessageReadedContent>() {
                }.getType());
                messageSyncService.readMark(messageContent);
            }else if(Objects.equals(command, MessageCommand.MSG_RECIVE_ACK.getCommand())){

//                接收方收到消息ack
//                MessageReadedContent messageContent = JSON.parseObject(msg, new TypeReference<MessageReadedContent>() {
//                }.getType());
//                messageSyncService.readMark(messageContent);
            }

            channel.basicAck(deliveryTag,false);

        }catch (Exception e){
            logger.error("处理消息出现异常：{}",e.getMessage());
            logger.error("RMQ_CHAT_TRAN_ERROR", e);
            logger.error("NACK_MSG:{}", msg);
            //第一个false 表示不批量拒绝，第二个false表示不重回队列
            channel.basicNack(deliveryTag, false, false);
        }finally {
            long end = System.currentTimeMillis();
            logger.debug("channel {} basic-Ack ,it costs {} ms,threadName = {},threadId={}", channel, end - start, t.getName(), t.getId());
        }

    }


}
