package com.lld.im.publish;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.proto.Msg;
import com.lld.im.proto.MsgBody;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author: Chackylee
 * @description: 发送mq消息到消息服务
 * @create: 2022-05-05 16:49
 **/
@Component
public class MqMessageProducer implements CommandLineRunner {

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * @description 往消息服务投递消息
     * @author chackylee
     * @date 2022/5/5 16:52
     * @param [msgBody]
     * @return void
    */
    public void sendMsg(MsgBody msgBody){
        rabbitTemplate.convertAndSend("Pipeline2MessageService",
                "", JSONObject.toJSONString(msgBody));
    }

    /**
     * @description 绑定交换机和队列
     * @author chackylee
     * @date 2022/5/5 16:55
     * @param [args]
     * @return void
    */
    @Override
    public void run(String... args) throws Exception {
        RabbitAdmin admin = new RabbitAdmin(this.rabbitTemplate.getConnectionFactory());
        Queue queue = new Queue("Pipeline2MessageService", true, false, false);
        DirectExchange exchange = new DirectExchange("Pipeline2MessageService", true, false);//交换机与队列同名
        Binding binding = BindingBuilder.bind(queue).to(exchange).with("");
        admin.declareQueue(queue);
        admin.declareExchange(exchange);
        admin.declareBinding(binding);
        //当一个服务同时作为消息的发送端和接收端时，建议使用不同的Connection避免一方出现故障或者阻塞影响另一方
        rabbitTemplate.setUsePublisherConnection(true);
    }
}
