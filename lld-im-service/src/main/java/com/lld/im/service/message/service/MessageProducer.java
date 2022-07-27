package com.lld.im.service.message.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.proto.Message;
import com.lld.im.codec.proto.MessagePack;
import com.lld.im.common.ClientType;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.Command;
import com.lld.im.common.enums.ImConnectStatusEnum;
import com.lld.im.common.enums.MessageCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.UserSession;
import com.lld.im.common.model.msg.MessageAck;
import com.lld.im.service.utils.UserSessionUtils;
import org.apache.catalina.manager.util.SessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/23
 * @version: 1.0
 */
@Service
public class MessageProducer {

    private static Logger logger = LoggerFactory.getLogger(MessageProducer.class);

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    UserSessionUtils userSessionUtils;

    private String queueName = Constants.RabbitConstants.MessageService2Im;

    private boolean containsMobilePhoneClient(List<UserSession> sessionList) {
        for (UserSession userSessionDto : sessionList) {
            Integer resourceType = userSessionDto.getClientType();
            if(Objects.equals(resourceType, ClientType.IOS.getCode())
                    ||Objects.equals(resourceType,ClientType.ANDROID.getCode())){
                return true;
            }
        }
        return false;
    }

    private List<UserSession> pipeLineConnectedSessions(String userId,Integer appId) {
        List<UserSession> sessionList = new ArrayList<UserSession>();

        List<UserSession> userSession = userSessionUtils.getUserSession(userId, appId);
        for (UserSession session : userSession) {
            if (!UserSessionUtils.isValid(session)) {
                continue;
            }
            if (StringUtils.isEmpty(session.getPipelineHost())) {
                //如果找不到用户连接的管道地址 则不发消息
                continue;
            }
            if (!Objects.equals(ImConnectStatusEnum.ONLINE_STATUS.getCode(), session.getConnectState())) {    //加枚举
                //会话如果是保存长连接状态，才会发消息。
                continue;
            }
            sessionList.add(session);
        }
        return sessionList;
    }

    /**
     * @description
     * @author chackylee
     * @date 2022/7/27 9:34
     * @param [toId, command, bizData, session] 
     * @return boolean
    */
    private boolean sendPack(String toId , Command command, Object bizData, UserSession session){

//        if (Objects.equals(session.getConnectState(), ImConnectStatusEnum.OFFLINE_STATUS.getCode()) ) {
//            //如果待接收端已处于PUSH_OFFLINE_STATE状态，则他只接收 REPEAT_LOGIN类型的消息 &&(command.getCommand() != MQAccountOperateType.FORCE_LOGOUT.getCommand()&& command.getCommand() != MQAccountOperateType.REPEATLOGIN.getCommand())
//            logger.info("session {} in PUSH_OFFLINE_STATE,the msg-passage was only open for REPEAT_LOGIN ", session);
//            return false;
//        }

        //构造消息内容
        MessagePack msgPack = new MessagePack<>();
        msgPack.setCommand(command.getCommand());
        msgPack.setToId(toId);
        msgPack.setClientType(session.getClientType());
        msgPack.setAppId(session.getAppId());
        msgPack.setImei(session.getImei());

        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(bizData));
        //msgId要特殊处理，如果是回包消息，data里不需要填充msgId,把里层的msgId赋值给外层。
        //如果是其他类型的消息，data里面和外面的 msgId需要保持一致，如果data里没有msgId,那么需要生成一个插入。
        if(command == MessageCommand.MSG_ACK || command == MessageCommand.GROUP_MSG_ACK) {
            ResponseVO<MessageAck> ackData = (ResponseVO<MessageAck>) bizData;
            String ackMsgId = ackData.getData().getMessageId();
            msgPack.setMsgId(ackMsgId);
        }else{
            String bizMsgId = jsonObject.getString("msgId");
            if(StringUtils.isEmpty(bizMsgId)){
                String genUid = UUID.randomUUID().toString().replace("-","");
                jsonObject.put("msgId",genUid);
                msgPack.setMsgId(genUid);
            }else{
                msgPack.setMsgId(bizMsgId);
            }
        }
//
        msgPack.setData(jsonObject);
        //发送消息
        String msg = JSON.toJSONString(msgPack);
        sendMessage(session, msg);
        return true;
    }

    /**
     * 发布消息至user下指定clientInfo的设备(如果在线的话）
     */
    public boolean sendToUserAppointedClient(String toId ,Command command, Object data , ClientInfo clientInfo){
        UserSession userSession = userSessionUtils.getUserSession(toId, clientInfo.getClientType(), clientInfo.getImei(), clientInfo.getAppId());
        logger.info("ready to send pack to {},sessionList: {},data: {} ", toId, userSession, JSON.toJSONString(data));
//        if(userSession!=null&&Objects.equals(ImConnectStatusEnum.ONLINE_STATE.getCode(), userSession.getConnectState())){
//            return sendPack(toId,command,data,userSession);
//        }
//        return false;
        return sendPack(toId,command,data,userSession);
    }

    private boolean sendMessage(UserSession session, Object msg) {
        try {
            logger.debug("send MessagePack==" + msg);
            rabbitTemplate.convertAndSend(queueName, session.getMqRouteKey(), msg);
            //rabbitTemplate.convertAndSend(mqQueueName_msToChannel, "", msg);
            return true;
        } catch (Exception e) {
            logger.error("publish-error",e);
            return false;
        }
    }

}
