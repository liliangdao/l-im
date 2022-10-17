package com.lld.im.tcp.publish;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.command.CommandType;
import com.lld.im.tcp.utils.MqFactory;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author: Chackylee
 * @description: 发送mq消息到消息服务
 * @create: 2022-07-27 16:49
 **/
public class MqMessageProducer {


    public static void sendMessageByCommand(Object msgBody,Integer command) throws IOException, TimeoutException {
        Channel channel = null;
        try {

            String com = command.toString();

            String commandSub = com.substring(0, 1);
            CommandType commandType = CommandType.getCommandType(commandSub);
            String channelName = "";
            if(commandType == CommandType.MESSAGE){
                channelName = Constants.RabbitConstants.Im2MessageService;
            }else if(commandType == CommandType.GROUP){
                channelName = Constants.RabbitConstants.Im2GroupService;
            }else if(commandType == CommandType.FRIEND){
                channelName = Constants.RabbitConstants.Im2FriendshipService;
            }else if(commandType == CommandType.USER){
                channelName = Constants.RabbitConstants.Im2UserService;
            }

            channel = MqFactory.getChannel(channelName);
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

        }

    }

}



