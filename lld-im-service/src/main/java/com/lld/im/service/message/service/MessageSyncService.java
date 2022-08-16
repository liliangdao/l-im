package com.lld.im.service.message.service;

import com.lld.im.codec.pack.BasePack;
import com.lld.im.codec.pack.MessageReadedAck;
import com.lld.im.codec.pack.MessageReadedPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.service.conversation.service.ConversationService;
import com.lld.im.service.utils.ShareThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-08-16 14:16
 **/
@Service
public class MessageSyncService {

    @Autowired
    private ShareThreadPool shareThreadPool;

    @Autowired
    ConversationService conversationService;

    @Autowired
    MessageProducer messageProducer;

    private static Logger logger = LoggerFactory.getLogger(MessageSyncService.class);

    /**
     * @description 消息已读，更新会话已读的seq，并且通知在线的其他端。
     * @author chackylee
     * @date 2022/8/16 14:35
     * @param [messageReaded]
     * @return void
    */
    public void readMark(MessageReadedPack messageReaded) {
        shareThreadPool.submit(() -> {
            conversationService.msgMarkRead(messageReaded);
            MessageReadedAck ack = new MessageReadedAck();
            BeanUtils.copyProperties(messageReaded, ack);
            ack(messageReaded, ack, messageReaded.getFromId());
//            //同步给其他端
            syncToSender(messageReaded);
        });
    }

    private void ack(BasePack clientInfo, MessageReadedAck readAck, String fromId) {
        ResponseVO<Object> wrappedResp = ResponseVO.successResponse(readAck);
        messageProducer.sendToUserAppointedClient(fromId, MessageCommand.MSG_READED_ACK, wrappedResp,
                new ClientInfo(clientInfo.getAppId(),clientInfo.getClientType(),clientInfo.getImei()));
    }

    private void syncToSender(MessageReadedPack messageReaded) {
        ClientInfo clinetInfo = new ClientInfo(messageReaded.getAppId(), messageReaded.getClientType(), messageReaded.getImei());
        messageProducer.sendToUserExceptClient(messageReaded.getFromId(), MessageCommand.MSG_READED_NOTIFY, messageReaded
                , clinetInfo);
    }

}
