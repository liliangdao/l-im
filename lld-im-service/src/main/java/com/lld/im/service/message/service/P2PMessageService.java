package com.lld.im.service.message.service;

import com.lld.im.codec.pack.ChatMessageAck;
import com.lld.im.codec.pack.P2PMessagePack;
import com.lld.im.codec.proto.Message;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ConversationTypeEnum;
import com.lld.im.common.enums.command.Command;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.msg.*;
import com.lld.im.service.conversation.service.ConversationService;
import com.lld.im.service.message.model.req.SendMessageReq;
import com.lld.im.service.message.model.resp.SendMessageResp;
import com.lld.im.service.service.seq.Seq;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.utils.UserSessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
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
    ConversationService conversationService;

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

        //?????????????????????????????????????????????????????????
        MessageContent p2pMessage = messageStoreService.getMessageFromMessageIdCache(chatMessageData.getMessageId(),chatMessageData.getAppId());
        if(chatMessageData.getMessageKey() != null || p2pMessage != null){

            //????????????????????????????????????????????????????????????????????????????????????ack???????????????????????????
            if(chatMessageData.getMessageSequence() == 0L){
                chatMessageData.setMessageSequence(p2pMessage.getMessageSequence());
            }
            threadPoolExecutor.execute(() -> {
                ack(chatMessageData, ResponseVO.successResponse());
                //????????????
                OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
                BeanUtils.copyProperties(chatMessageData,offlineMessageContent);
                offlineMessageContent.setConversationType(ConversationTypeEnum.P2P.getCode());
                messageStoreService.storeOffLineMessage(offlineMessageContent);
                List<ClientInfo> clientInfos = dispatchMessage(p2pMessage, chatMessageData.getOfflinePushInfo());
                if(clientInfos.isEmpty()){
                    //??????????????????????????????????????????ack????????????
                    revicerAck(chatMessageData,true);
                }
            });
            return;
        }

        ResponseVO responseVO = imServerpermissionCheck(fromId, toId, chatMessageData.getAppId());

        if (responseVO.isOk()) {
            long seq = this.seq.getSeq(chatMessageData.getAppId() + ":" + Constants.SeqConstants.Message);
            chatMessageData.setMessageSequence(seq);
            //??????+??????+??????????????????????????????????????????????????????
            threadPoolExecutor.execute(() -> {
                doProcessMessage(chatMessageData);
            });
        } else {
            ack(chatMessageData, responseVO);
        }

    }

    /**
     * @description: ????????????????????????
     * @param
     * @return void
     * @author lld
     * @since 2022/9/18
     */
    public void doProcessMessage(ChatMessageContent chatMessageData){
        //??????????????????msgBody
        Long messageKey = messageStoreService.storeP2PMessage(chatMessageData);
        chatMessageData.setMessageKey(messageKey);
        //??????
        ack(chatMessageData,ResponseVO.successResponse());

        syncToSender(chatMessageData,chatMessageData,chatMessageData.getOfflinePushInfo());

        //???????????????redis
        OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
        BeanUtils.copyProperties(chatMessageData,offlineMessageContent);
        offlineMessageContent.setConversationType(ConversationTypeEnum.P2P.getCode());
        messageStoreService.storeOffLineMessage(offlineMessageContent);

        //????????????
        List<ClientInfo> clientInfos = dispatchMessage(chatMessageData, chatMessageData.getOfflinePushInfo());

        messageStoreService.setMessageFromMessageIdCache(chatMessageData);

        if(clientInfos.isEmpty()){
            //??????????????????????????????????????????ack????????????
            revicerAck(chatMessageData,true);
        }
    }


    public SendMessageResp send(SendMessageReq req){

        SendMessageResp sendMessageResp = new SendMessageResp();

        ChatMessageContent message = new ChatMessageContent();
        BeanUtils.copyProperties(req,message);
        long seq = this.seq.getSeq(req.getAppId() + ":" + Constants.SeqConstants.Message);
        message.setMessageSequence(seq);

        Long messageKey = messageStoreService.storeP2PMessage(message);

        //???????????????redis
        OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
        BeanUtils.copyProperties(message,offlineMessageContent);
        offlineMessageContent.setConversationType(ConversationTypeEnum.P2P.getCode());
        messageStoreService.storeOffLineMessage(offlineMessageContent);

        sendMessageResp.setMessageKey(messageKey);
        sendMessageResp.setMessageTime(System.currentTimeMillis());

        dispatchMessage(message, req.getOfflinePushInfo());
        syncToSender(message,message,req.getOfflinePushInfo());

        return sendMessageResp;
    }


    /**
     * @param content result
     * @return void
     * @description ack?????????????????????????????????
     * @author chackylee
     * @date 2022/7/22 16:29
     */
    private void ack(MessageContent content, ResponseVO result) {
        logger.debug("result = {}",result);
        logger.info("msg ack,msgId = {},msgSeq ={}???checkResult = {}", content.getMessageId(), content.getMessageSequence(), result);
        ChatMessageAck ackData = new ChatMessageAck(content.getMessageId(), content.getMessageSequence());
        result.setData(ackData);
        messageProducer.sendToUserAppointedClient(content.getFromId(), MessageCommand.MSG_ACK, result, content);
    }

    /**
     * @description ack??????????????????????????????
     * @author chackylee
     * @date 2022/9/26 11:16
     * @param content, result, command
     * @return void
    */
    public void revicerAck(MessageContent content,Boolean serverSend) {
        logger.info("msg revicerAck,msgId = {},msgSeq ={}", content.getMessageId(), content.getMessageSequence());
        ChatMessageAck ackData = new ChatMessageAck(content.getMessageId(), content.getMessageSequence());
        ackData.setServerSend(true);
        MessageReciveAckContent messageReciveAckContent = new MessageReciveAckContent();
        messageReciveAckContent.setToId(content.getFromId());
        messageReciveAckContent.setMessageSequence(content.getMessageSequence());
        messageReciveAckContent.setMessageKey(content.getMessageKey());
        messageReciveAckContent.setMessageId(content.getMessageId());
        messageReciveAckContent.setServerSend(serverSend);
        messageProducer.sendToUserAppointedClient(content.getFromId(), MessageCommand.MSG_RECIVE_ACK, ackData, content);
    }

    /**
     * @description ack??????????????????????????????
     * @author chackylee
     * @date 2022/9/26 11:16
     * @param [content, result, command]
     * @return void
     */
    public void revicerAck(MessageReciveAckContent content) {
        logger.info("msg revicerAck,msgId = {},msgSeq ={}", content.getMessageId(), content.getMessageSequence());
        messageProducer.sendToUserAppointedClient(content.getToId(), MessageCommand.MSG_RECIVE_ACK, content, content);
    }


    /**
     * @param fromId, toId, appId
     * @return com.lld.im.common.ResponseVO
     * @description ????????????-???????????????????????????????????????????????????????????????????????????????????? 0
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

        logger.debug("messageProducer.sendToUser msgId:{}, success sent Result ??? {}", messageContent.getMessageId(), successResults);

        // ???????????????session?????????????????????????????????????????????????????????
        if (!UserSessionUtils.containMobile(successResults)) {
            //???????????????????????????????????????????????????
//            pushService.pushOfflineInfo(offlinePushInfo, messageContent);
        }

//        if (!Objects.equals(0L, messageContent.getmess())) {
//            //????????????????????????????????????????????????????????????????????????????????????????????????
//            //??????LOGINMODE?????? successResults ???????????????????????????????????????
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
