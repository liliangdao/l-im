package com.lld.im.reciver;

import com.lld.im.constant.Constants;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: Chackylee
 * @description: 关系链服务监听
 * @create: 2022-05-19 15:20
 **/
@Component
public class FirendShipOperationListener {

    private static Logger logger = LoggerFactory.getLogger(FirendShipOperationListener.class);

    private final String mqQueryName = "";

    List<Queue> list = new ArrayList<>();

    @Value("${mqQueueName}")
    private String mqQueueName;


    /**
     * 交换机、队列不存在的话，以下注解可以自动创建交换机和队列
     *
     * @param headers
     * @param channel
     * @throws Exception
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "${spring.application.name}", durable = "true"),
            exchange = @Exchange(value = Constants.RabbitConstants.FriendShip2Im, durable = "true")
    ))
    @RabbitHandler
    public void onOrderMessage(@Payload Message  message,
                               @Headers Map<String, Object> headers,
                               Channel channel) throws Exception {

        logger.info("--------------收到消息，开始消费------------");
        String msg = new String(message.getBody(),"utf-8");
        logger.info("内容:"+ msg );
        Long deliveryTag = (Long) headers.get(AmqpHeaders.DELIVERY_TAG);

    }

//    @Override
//    public void run(String... args) throws Exception {
//        RabbitAdmin admin = new RabbitAdmin(this.rabbitTemplate.getConnectionFactory());
//        org.springframework.amqp.core.Queue queue = new Queue(Constants.RabbitConstants.Im2MsgService, true, false, false);
//        DirectExchange exchange = new DirectExchange(Constants.RabbitConstants.Im2MsgService, true, false);//交换机与队列同名
//        Binding binding = BindingBuilder.bind(queue).to(exchange).with("");
//        admin.declareQueue(queue);
//        admin.declareExchange(exchange);
//        admin.declareBinding(binding);
//        //当一个服务同时作为消息的发送端和接收端时，建议使用不同的Connection避免一方出现故障或者阻塞影响另一方
//        rabbitTemplate.setUsePublisherConnection(true);
//    }

}
