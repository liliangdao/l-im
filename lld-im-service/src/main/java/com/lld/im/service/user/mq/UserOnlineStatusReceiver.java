package com.lld.im.service.user.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.enums.command.UserEventCommand;
import com.lld.im.common.model.msg.ChatMessageContent;
import com.lld.im.common.model.msg.MessageReadedContent;
import com.lld.im.common.model.msg.MessageReciveAckContent;
import com.lld.im.service.message.mq.ChatOperateReceiver;
import com.lld.im.service.message.service.MessageSyncService;
import com.lld.im.service.message.service.P2PMessageService;
import com.lld.im.service.user.model.UserOnlineStatusChangeContent;
import com.lld.im.service.user.model.UserOnlineStatusSubscribeContent;
import com.lld.im.service.user.service.UserStatusService;
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
 * @create: 2022-07-22 15:48
 **/
@Component
public class UserOnlineStatusReceiver {

    private static Logger logger = LoggerFactory.getLogger(ChatOperateReceiver.class);

    @Autowired
    UserStatusService userStatusServicel;

    /**
     * 订阅MQ单聊消息队列--处理
     *
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = Constants.RabbitConstants.Im2UserService, durable = "true"),
            exchange = @Exchange(value = Constants.RabbitConstants.Im2UserService, durable = "true")
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

            //1.在线状态变更通知 上线由客户端发起。下线由服务端发起。
            if (Objects.equals(command, UserEventCommand.USER_ONLINE_STATUS_CHANGE.getCommand())) {
                UserOnlineStatusChangeContent content = JSON.parseObject(msg, new TypeReference<UserOnlineStatusChangeContent>() {
                }.getType());
                userStatusServicel.processUserLoginNotify(content);
            }
            //用户在线状态订阅，一般是临时订阅
            else if (Objects.equals(command, UserEventCommand.USER_ONLINE_STATUS_SUBSCRIBE.getCommand())) {
                UserOnlineStatusSubscribeContent content = JSON.parseObject(msg, new TypeReference<UserOnlineStatusSubscribeContent>() {
                }.getType());
                userStatusServicel.processUserSubscribeNotify(content);

            }//拉取在线状态信息
            else if (Objects.equals(command, UserEventCommand.PULL_USER_ONLINE_STATUS.getCommand())) {
                UserOnlineStatusChangeContent content = JSON.parseObject(msg, new TypeReference<UserOnlineStatusChangeContent>() {
                }.getType());

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
