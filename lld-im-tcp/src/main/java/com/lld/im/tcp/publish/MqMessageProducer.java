package com.lld.im.tcp.publish;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.constant.Constants;
import com.lld.im.tcp.utils.MqFactoryUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author: Chackylee
 * @description: 发送mq消息到消息服务
 * @create: 2022-07-27 16:49
 **/
public class MqMessageProducer {

    /**
     * @param [msgBody]
     * @return void
     * @description 往消息服务投递消息
     * @author chackylee
     * @date 2022/5/5 16:52
     */
    public static void sendMessageToMessageService(Object msgBody) throws IOException, TimeoutException {
        Channel channel = null;
        try {
            Connection connection = MqFactoryUtils.getConnection();
            //创建通信“通道”，相当于TCP中的虚拟连接
            channel = connection.createChannel();
            //四个参数
            //exchange 交换机，暂时用不到，在后面进行发布订阅时才会用到
            //队列名称
            //额外的设置属性
            //最后一个参数是要传递的消息字节数组
            channel.basicPublish(Constants.RabbitConstants.Im2MessageService
                    , "", null, JSONObject.toJSONString(msgBody).getBytes());
            System.out.println("===发送成功===");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            if (channel != null) {
//                channel.close();
//                channel.close();
//            }

        }

    }

//    /**
//     * @description 绑定交换机和队列
//     * @author chackylee
//     * @date 2022/5/5 16:55
//     * @param [args]
//     * @return void
//    */
//    @Override
//    public void run(String... args) throws Exception {
//        RabbitAdmin admin = new RabbitAdmin(this.rabbitTemplate.getConnectionFactory());
//        Queue queue = new Queue(Constants.RabbitConstants.Im2MsgService, true, false, false);
//        DirectExchange exchange = new DirectExchange(Constants.RabbitConstants.Im2MsgService, true, false);//交换机与队列同名
//        Binding binding = BindingBuilder.bind(queue).to(exchange).with("");
//        admin.declareQueue(queue);
//        admin.declareExchange(exchange);
//        admin.declareBinding(binding);
//        //当一个服务同时作为消息的发送端和接收端时，建议使用不同的Connection避免一方出现故障或者阻塞影响另一方
//        rabbitTemplate.setUsePublisherConnection(true);
//    }
}



