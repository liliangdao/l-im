package com.lld.im.service.message.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.pack.message.ChatMessageAck;
import com.lld.im.codec.proto.MessagePack;
import com.lld.im.common.ClientType;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.command.Command;
import com.lld.im.common.enums.command.GroupEventCommand;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.UserSession;
import com.lld.im.service.utils.UserSessionUtils;
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
        if(command == MessageCommand.MSG_ACK || command == GroupEventCommand.GROUP_MSG_ACK) {
            ResponseVO<ChatMessageAck> ackData = (ResponseVO<ChatMessageAck>) bizData;
            String ackMsgId = ackData.getData().getMessageId();
            msgPack.setMessageId(ackMsgId);
        }else{
            String bizMsgId = jsonObject.getString("messageId");
            if(StringUtils.isEmpty(bizMsgId)){
                String genUid = UUID.randomUUID().toString().replace("-","");
                jsonObject.put("msgId",genUid);
                msgPack.setMessageId(genUid);
            }else{
                msgPack.setMessageId(bizMsgId);
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
     * @return 返回发送成功的session
     */
    public void sendToUser(String toId,Integer clientType,String imel, Command command, Object data,Integer appId) {
        if(clientType != null && StringUtils.isBlank(imel)){
            ClientInfo clientInfo = new ClientInfo(appId,clientType,imel);
            this.sendToUserExceptClient(toId,command,data,clientInfo);
            //发给除了这个端的人
        }else {
            //发给所有人
            this.sendToUser(toId, command, data, appId);
        }
    }

    /**
     * @return 返回发送成功的session
     */
    public List<ClientInfo> sendToUser(String toId, Command command, Object data,Integer appId) {

        List<UserSession> sessionList = pipeLineConnectedSessions(toId,appId);
        logger.info("ready to send pack to {},sessionList: {},data: {} ", toId, sessionList, JSON.toJSONString(data));

        List<ClientInfo> successResults = new ArrayList<>();
        for (UserSession session : sessionList) {
            boolean sendOk = sendPack(toId, command, data, session);
            if (sendOk) {
                successResults.add(new ClientInfo(session.getAppId(),session.getClientType(),session.getImei()));
            }
        }
        return successResults;
    }

    private List<UserSession> getSessionsExceptClient(List<UserSession> sessionList, ClientInfo clientInfo) {
        List<UserSession> results = new ArrayList<>();
        for (UserSession session : sessionList) {
            if(!isMatch(session, clientInfo)){
                results.add(session);
            }
        }
        return results;
    }

    /**
     * 发布消息至user下指定clientInfo的设备
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

    /**
     *  发布消息至user下所有session,除了指定clientInfo的设备.
     */
    public void sendToUserExceptClient(String toId ,Command command, Object data , ClientInfo clientInfo){
        List<UserSession> sessionList = pipeLineConnectedSessions(toId,clientInfo.getAppId());
        for (UserSession session : getSessionsExceptClient(sessionList, clientInfo)) {
            sendPack(toId,command,data,session);
        };
    }

    /**
     * 发布消息至user下指定clientType的设备
     */
    public List<ClientInfo> sendToUserAppointedClient(String toId ,Command command, Object data , int clientType,Integer appId){

        List<UserSession> sessionList = pipeLineConnectedSessions(toId,appId);
        sessionList.removeIf(o->!o.getClientType().equals(clientType));

        logger.info("ready to send pack to {},sessionList: {},data: {} ", toId, sessionList, JSON.toJSONString(data));

        List<ClientInfo> successResults = new ArrayList<>();
        for (UserSession session : sessionList) {
            boolean sendOk = sendPack(toId, command, data, session);
            if (sendOk) {
                successResults.add(new ClientInfo(session.getAppId(),session.getClientType(),session.getImei()));
            }
        }
        return successResults;
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

    private boolean isMatch(UserSession sessionDto, ClientInfo clientInfo) {
        return Objects.equals(sessionDto.getAppId(), clientInfo.getAppId())
                && Objects.equals(sessionDto.getImei(), clientInfo.getImei())
                && Objects.equals(sessionDto.getClientType(), clientInfo.getClientType());
    }

}
