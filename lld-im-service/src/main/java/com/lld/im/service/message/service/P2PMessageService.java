package com.lld.im.service.message.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.msg.ChatMessageContent;
import com.lld.im.common.model.msg.MessageAck;
import com.lld.im.service.service.seq.Seq;
import com.lld.im.service.user.service.ImUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
                messageStoreService.storeMessage(chatMessageData);
                //回包
                ack(chatMessageData,ResponseVO.successResponse());
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
    private void ack(ChatMessageContent content, ResponseVO result) {
        logger.info("result = {}",result);
        logger.info("msg ack,msgId = {},msgSeq ={}，checkResult = {}", content.getMessageId(), content.getMessageSequence(), result);
        MessageAck ackData = new MessageAck(content.getMessageId(), content.getMessageSequence());
        result.setData(ackData);
//        messageProducer.sendToUserAppointedClient(fromId, MQChatOperateType.MSG_ACK, wrappedResp, clientInfo);
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
        return ResponseVO.successResponse();
    }

}
