package com.lld.im.service.message.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.MessageCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.msg.*;
import com.lld.im.service.service.seq.Seq;
import com.lld.im.service.user.service.ImUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

        long t0 = System.currentTimeMillis();
        String fromId = chatMessageData.getFromId();
        String toId = chatMessageData.getToId();

        ResponseVO responseVO = imServerpermissionCheck(fromId, toId, chatMessageData.getAppId());

        if (responseVO.isOk()) {
            long seq = this.seq.getSeq(Constants.SeqConstants.Message);
            chatMessageData.setMessageSequence(seq);
            //落库+回包+分发（发送给同步端和接收方的所有端）
            threadPoolExecutor.execute(() -> {
                //插入历史库和msgBody
                String messageKey = messageStoreService.storeMessage(chatMessageData);
                chatMessageData.setMessageKey(messageKey);
                //回包
                ack(chatMessageData,ResponseVO.successResponse());

                P2PMessageContent p2PMessageContent = extractP2PMessage(chatMessageData);
                //插入离线库
                messageStoreService.storeOffLineMessage(p2PMessageContent);

                //消息分发 给同步端和接收方
                dispatchMessage(p2PMessageContent,chatMessageData.getOfflinePushInfo());
            });
        } else {
            ack(chatMessageData, responseVO);
        }

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
        MessageAck ackData = new MessageAck(content.getMessageId(), content.getMessageSequence());
        result.setData(ackData);
        messageProducer.sendToUserAppointedClient(content.getFromId(), MessageCommand.MSG_ACK, result, content);
    }


    /**
     * @param fromId, toId, appId
     * @return com.lld.im.common.ResponseVO
     * @description 消息收发-双方关系校验（好友、拉黑关系、禁收禁发等），校验通过返回 0
     * @author chackylee
     * @date 2022/7/22 16:01
     */
    public ResponseVO imServerpermissionCheck(String fromId, String toId, Integer appId) {

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

    public void dispatchMessage(P2PMessageContent messageContent, OfflinePushInfo offlinePushInfo) {

        logger.debug("dispatchMessage : {}", messageContent);
        String toId = messageContent.getToId();

        P2PMessageContent p2PMessagePack = new P2PMessageContent();
        BeanUtils.copyProperties(messageContent, p2PMessagePack);

        if (p2PMessagePack.getMessageLifeTime() != null && p2PMessagePack.getMessageLifeTime() != 0) {
            p2PMessagePack.setMessageLifeTime(0L);
        }

        List<ClientInfo> successResults = messageProducer.sendToUser(toId, MessageCommand.MSG_P2P, p2PMessagePack,messageContent.getAppId());

        logger.debug("messageProducer.sendToUser msgId:{}, success sent Result ： {}", messageContent.getMessageId(), successResults);

        // 如果成功的session列表中不包括手机，则需要推送离线消息。
//        if (!SessionUtil.containMobile(successResults)) {
//            //如果发送失败，则推送离线消息，并入同步库
//            pushService.pushOfflineInfo(offlinePushInfo, messageContent);
//        }

//        if (!Objects.equals(0L, messageContent.getmess())) {
//            //根据多端登陆模式的配置。决定未发送成功的消息存到哪几张离线消息表
//            //根据LOGINMODE，和 successResults 决定哪几张表需要存同步消息
//            Set<SyncTerminalEnum> terminalsNotInSessions = SessionUtil.findTerminalsNotInSessions(successResults,messageContent.getAppId());
//            syncMessageService.syncPeerToPeerReceiveMsg(messageContent, terminalsNotInSessions);
//        }
    }

    public P2PMessageContent extractP2PMessage(MessageContent messageContent){
        P2PMessageContent p2PMessagePack = new P2PMessageContent();
        BeanUtils.copyProperties(messageContent, p2PMessagePack);
        return p2PMessagePack;
    }


}
