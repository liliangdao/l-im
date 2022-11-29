package com.lld.im.service.message.service;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.pack.message.ChatMessageAck;
import com.lld.im.codec.pack.message.MessageReadedAck;
import com.lld.im.codec.pack.message.P2PMessagePack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ConversationTypeEnum;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.msg.*;
import com.lld.im.service.conversation.service.ConversationService;
import com.lld.im.service.message.model.req.SendMessageReq;
import com.lld.im.service.message.model.resp.SendMessageResp;
import com.lld.im.service.service.seq.Seq;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.utils.CallbackService;
import com.lld.im.service.utils.ConversationIdGenerate;
import com.lld.im.service.utils.UserSessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.beans.BeanUtils.copyProperties;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-22 15:49
 **/
@Service
public class P2PMessageService {

    private static Logger logger = LoggerFactory.getLogger(P2PMessageService.class);

    @Autowired
    ImUserService imUserService;

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    @Qualifier("redisSeq")
    Seq seq;

    @Autowired
    MessageStoreService messageStoreService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    CallbackService callbackService;

    @Autowired
    ConversationService conversationService;

    @Autowired
    AppConfig appConfig;

    private final ThreadPoolExecutor threadPoolExecutor;
    {
        final AtomicInteger tNum = new AtomicInteger(0);

        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(2 << 20), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("Message-P2P-Processor-" + tNum.getAndIncrement());
                return t;
            }
        });

    }

    public void process(ChatMessageContent chatMessageData) {

        String fromId = chatMessageData.getFromId();
        String toId = chatMessageData.getToId();

        //从换从中获取消息如果存在直接分发和回包
        MessageContent p2pMessage = messageStoreService.getMessageFromMessageIdCache(chatMessageData.getMessageId(),chatMessageData.getAppId());
        if(chatMessageData.getMessageKey() != null || p2pMessage != null){

            //表示是客户端重发的消息并且服务端已处理完成，可能是没收到ack，直接回包，并分发
            if(chatMessageData.getMessageSequence() == 0L){
                chatMessageData.setMessageSequence(p2pMessage.getMessageSequence());
            }
            threadPoolExecutor.execute(() -> {
                ack(chatMessageData, ResponseVO.successResponse());
                //消息分发
                OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
                BeanUtils.copyProperties(chatMessageData,offlineMessageContent);
                offlineMessageContent.setConversationType(ConversationTypeEnum.P2P.getCode());
                messageStoreService.storeOffLineMessage(offlineMessageContent);
                List<ClientInfo> clientInfos = dispatchMessage(p2pMessage, chatMessageData.getOfflinePushInfo());
                if(clientInfos.isEmpty()){
                    //服务端代替客户端发送消息确认ack给发送方
                    revicerAck(chatMessageData,true);
                }
            });
            return;
        }

        //校验权限
        ResponseVO responseVO = imServerpermissionCheck(fromId, toId, chatMessageData.getAppId());
        if(!responseVO.isOk()){
            ack(chatMessageData, responseVO);
            return;
        }
        //回调
        if(appConfig.isSendMessageAfterCallback()){
            responseVO = callbackService.beforeCallback(chatMessageData.getAppId(), Constants.CallbackCommand.SendMessageBefore
                    , JSONObject.toJSONString(chatMessageData));
        }

        if (responseVO.isOk()) {
            long seq = this.seq.getSeq(chatMessageData.getAppId() + ":" +
                    Constants.SeqConstants.Message + ":" +
                    ConversationIdGenerate.generateP2PId(chatMessageData.getFromId(),chatMessageData.getToId()));
            chatMessageData.setMessageSequence(seq);
            //落库+回包+分发（发送给同步端和接收方的所有端）
            threadPoolExecutor.execute(() -> {
                doProcessMessage(chatMessageData);
            });
        } else {
            ack(chatMessageData, responseVO);
        }

    }

    /**
     * @description: 核心处理消息方法
     * @param
     * @return void
     * @author lld
     * @since 2022/9/18
     */
    public void doProcessMessage(ChatMessageContent chatMessageData){
        //插入历史库和msgBody TODO 改为异步存储，这里只分配id
        Long messageKey = messageStoreService.storeP2PMessage(chatMessageData);
        chatMessageData.setMessageKey(messageKey);
        //回包
        ack(chatMessageData,ResponseVO.successResponse());

        syncToSender(chatMessageData,chatMessageData,chatMessageData.getOfflinePushInfo());

        //插入离线库redis
        OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
        BeanUtils.copyProperties(chatMessageData,offlineMessageContent);
        offlineMessageContent.setConversationType(ConversationTypeEnum.P2P.getCode());
        messageStoreService.storeOffLineMessage(offlineMessageContent);

        //消息分发
        List<ClientInfo> clientInfos = dispatchMessage(chatMessageData, chatMessageData.getOfflinePushInfo());

        messageStoreService.setMessageFromMessageIdCache(chatMessageData);

        if(clientInfos.isEmpty()){
            //服务端代替客户端发送消息确认ack给发送方
            revicerAck(chatMessageData,true);
        }

        if(appConfig.isSendMessageAfterCallback()){
            callbackService.callback(chatMessageData.getAppId(),Constants.CallbackCommand.SendMessageAfter,
                    JSONObject.toJSONString(chatMessageData));
        }
    }

    public SendMessageResp send(SendMessageReq req){

        SendMessageResp sendMessageResp = new SendMessageResp();

        ChatMessageContent message = new ChatMessageContent();
        BeanUtils.copyProperties(req,message);
        long seq = this.seq.getSeq(req.getAppId() + ":" + Constants.SeqConstants.Message);
        message.setMessageSequence(seq);

        Long messageKey = messageStoreService.storeP2PMessage(message);

        //插入离线库redis
        OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
        BeanUtils.copyProperties(message,offlineMessageContent);
        offlineMessageContent.setConversationType(ConversationTypeEnum.P2P.getCode());
        messageStoreService.storeOffLineMessage(offlineMessageContent);

        sendMessageResp.setMessageKey(messageKey);
        sendMessageResp.setMessageId(req.getMessageId());
        sendMessageResp.setMessageTime(System.currentTimeMillis());

        dispatchMessage(message, req.getOfflinePushInfo());
        syncToSender(message,message,req.getOfflinePushInfo());

        return sendMessageResp;
    }


    /**
     * @param content result
     * @return void
     * @description ack回包，消息发给指定的端
     * @author chackylee
     * @date 2022/7/22 16:29
     */
    private void ack(MessageContent content, ResponseVO result) {
        logger.debug("result = {}",result);
        logger.info("msg ack,msgId = {},msgSeq ={}，checkResult = {}", content.getMessageId(), content.getMessageSequence(), result);
        ChatMessageAck ackData = new ChatMessageAck(content.getMessageId(), content.getMessageSequence());
        result.setData(ackData);
        messageProducer.sendToUserAppointedClient(content.getFromId(), MessageCommand.MSG_ACK, result, content);
    }

    /**
     * @description 消息接收确认ack，服务端发给发送方
     * @author chackylee
     * @date 2022/9/26 11:16
     * @param content, result, command
     * @return void
    */
    public void revicerAck(MessageContent content,Boolean serverSend) {
        logger.info("msg revicerAck,msgId = {},msgSeq ={}", content.getMessageId(), content.getMessageSequence());
        MessageReciveAckContent messageReciveAckContent = new MessageReciveAckContent();
        messageReciveAckContent.setFromId(content.getFromId());
        messageReciveAckContent.setMessageSequence(content.getMessageSequence());
        messageReciveAckContent.setMessageKey(content.getMessageKey());
        messageReciveAckContent.setMessageId(content.getMessageId());
        messageReciveAckContent.setServerSend(serverSend);
        messageProducer.sendToUserAppointedClient(content.getFromId(), MessageCommand.MSG_RECIVE_ACK, messageReciveAckContent, content);
    }



    /**
     * @param fromId, toId, appId
     * @return com.lld.im.common.ResponseVO
     * @description 消息收发-双方关系校验（好友、拉黑关系、禁收禁发等），校验通过返回 0
     * @author chackylee
     * @date 2022/7/22 16:01
     */
    private ResponseVO imServerpermissionCheck(String fromId, String toId, Integer appId) {

        ResponseVO checkForbidden = checkSendMessageService.checkUserForbidAndMute(fromId, toId, appId);
        if (!checkForbidden.isOk()) {
            return checkForbidden;
        }

        ResponseVO friendCheck = checkSendMessageService.checkFriendShip(fromId, toId, appId);

        if (!friendCheck.isOk()) {
            return friendCheck;
        }
        return ResponseVO.successResponse();
    }

    private void syncToSender(MessageContent content,ClientInfo clientInfo, OfflinePushInfo offlinePushInfo) {
        messageProducer.sendToUserExceptClient(content.getFromId(),MessageCommand.MSG_P2P,content,clientInfo);
    }

    private List<ClientInfo> dispatchMessage(MessageContent messageContent, OfflinePushInfo offlinePushInfo) {

        logger.debug("dispatchMessage : {}", messageContent);
        String toId = messageContent.getToId();

        P2PMessagePack p2PMessagePack = new P2PMessagePack();
        BeanUtils.copyProperties(messageContent, p2PMessagePack);

//        if (p2PMessagePack.getMessageLifeTime() != null && p2PMessagePack.getMessageLifeTime() != 0) {
//            p2PMessagePack.setMessageLifeTime(0L);
//        }

        List<ClientInfo> successResults = messageProducer.sendToUser(toId, MessageCommand.MSG_P2P, p2PMessagePack,messageContent.getAppId());

        logger.debug("messageProducer.sendToUser msgId:{}, success sent Result ： {}", messageContent.getMessageId(), successResults);

        // 如果成功的session列表中不包括手机，则需要推送离线消息。
        if (!UserSessionUtils.containMobile(successResults)) {
            //如果接收端没有手机，则推送离线消息
//            pushService.pushOfflineInfo(offlinePushInfo, messageContent);
        }

//        if (!Objects.equals(0L, messageContent.getmess())) {
//            //根据多端登陆模式的配置。决定未发送成功的消息存到哪几张离线消息表
//            //根据LOGINMODE，和 successResults 决定哪几张表需要存同步消息
//            Set<SyncTerminalEnum> terminalsNotInSessions = SessionUtil.findTerminalsNotInSessions(successResults,messageContent.getAppId());
//            syncMessageService.syncPeerToPeerReceiveMsg(messageContent, terminalsNotInSessions);
//        }
        return successResults;
    }

//    private P2PMessagePack extractP2PMessage(MessageContent messageContent){
//        P2PMessageContent p2PMessagePack = new P2PMessageContent();
//        BeanUtils.copyProperties(messageContent, p2PMessagePack);
//        return p2PMessagePack;
//    }


}
