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
            channel = MqFactoryUtils.getChannel(Constants.RabbitConstants.Im2MessageService);
            //四个参数
            //exchange 交换机，暂时用不到，在后面进行发布订阅时才会用到
            //队列名称
            //额外的设置属性
            //最后一个参数是要传递的消息字节数组
            String data = JSONObject.toJSONString(msgBody);
            channel.basicPublish(Constants.RabbitConstants.Im2MessageService
                    , "", null, data.getBytes());
            System.out.println("===发送成功===" + data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            if (channel != null) {
//                channel.close();
//                channel.close();
//            }

        }

    }

    /**
     * @param [msgBody]
     * @return void
     * @description 往消息服务投递消息
     * @author chackylee
     * @date 2022/5/5 16:52
     */
    public static void sendMessageToMessageService(Object msgBody,Integer command) throws IOException, TimeoutException {
        Channel channel = null;
        try {
            channel = MqFactoryUtils.getChannel(Constants.RabbitConstants.Im2MessageService);
            //四个参数
            //exchange 交换机，暂时用不到，在后面进行发布订阅时才会用到
            //队列名称
            //额外的设置属性
            //最后一个参数是要传递的消息字节数组

            JSONObject o = (JSONObject)JSONObject.toJSON(msgBody);
            o.put("command",command);

            String data = o.toJSONString();
            channel.basicPublish(Constants.RabbitConstants.Im2MessageService
                    , "", null, data.getBytes());
            System.out.println("===发送成功===" + data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            if (channel != null) {
//                channel.close();
//                channel.close();
//            }

        }

    }

    /**
     * @param [msgBody]
     * @return void
     * @description 往消息服务投递消息
     * @author chackylee
     * @date 2022/5/5 16:52
     */
    public static void sendMessageToGroupService(Object msgBody,Integer command) throws IOException, TimeoutException {
        Channel channel = null;
        try {
            channel = MqFactoryUtils.getChannel(Constants.RabbitConstants.GroupService2Im);
            //四个参数
            //exchange 交换机，暂时用不到，在后面进行发布订阅时才会用到
            //队列名称
            //额外的设置属性
            //最后一个参数是要传递的消息字节数组

            JSONObject o = (JSONObject)JSONObject.toJSON(msgBody);
            o.put("command",command);

            String data = o.toJSONString();
            channel.basicPublish(Constants.RabbitConstants.Im2GroupService
                    , "", null, data.getBytes());
            System.out.println("===发送成功===" + data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            if (channel != null) {
//                channel.close();
//                channel.close();
//            }

        }

    }

    public static void sendMessageByCommand(Object msgBody,Integer command) throws IOException, TimeoutException {
        Channel channel = null;
        try {

            String com = command.toString();
            String channelName = "";
            if(com.startsWith("1")){
                channelName = Constants.RabbitConstants.Im2MessageService;
            }else if(com.startsWith("2")){
                channelName = Constants.RabbitConstants.Im2GroupService;
            }else if(com.startsWith("3")){
                channelName = Constants.RabbitConstants.Im2FriendshipService;
            }else if(com.startsWith("4")){
                channelName = Constants.RabbitConstants.Im2UserService;
            }

            channel = MqFactoryUtils.getChannel(channelName);
            //四个参数
            //exchange 交换机，暂时用不到，在后面进行发布订阅时才会用到
            //队列名称
            //额外的设置属性
            //最后一个参数是要传递的消息字节数组

            JSONObject o = (JSONObject)JSONObject.toJSON(msgBody);
            o.put("command",command);

            String data = o.toJSONString();
            channel.basicPublish(channelName
                    , "", null, data.getBytes());
            System.out.println("===发送成功===" + data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            if (channel != null) {
//                channel.close();
//                channel.close();
//            }

        }

    }

}



