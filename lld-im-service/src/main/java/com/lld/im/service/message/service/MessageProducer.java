package com.lld.im.service.message.service;

import com.lld.im.common.ClientType;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ImConnectStatusEnum;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.UserSession;
import com.lld.im.service.utils.UserSessionUtils;
import org.apache.catalina.manager.util.SessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/23
 * @version: 1.0
 */
@Service
public class MessageProducer {

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


//    private boolean isMatch(UserSession sessionDto, ClientInfo clientInfo) {
//        return Objects.equals(sessionDto.getAppId(), clientInfo.getAppId())
//                && Objects.equals(sessionDto.getImei(), clientInfo.getImei())
//                && Objects.equals(sessionDto.getClientType(), clientInfo.getClientType());
//    }

}
