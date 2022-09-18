package com.lld.im.service.message.service;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.pack.MessageReadedAck;
import com.lld.im.codec.pack.MessageReadedPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.SyncReq;
import com.lld.im.common.model.SyncResp;
import com.lld.im.common.model.msg.MessageReadedContent;
import com.lld.im.common.model.msg.MessageReciveAckContent;
import com.lld.im.common.model.msg.OfflineMessageContent;
import com.lld.im.service.conversation.service.ConversationService;
import com.lld.im.service.utils.ShareThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    @Autowired
    RedisTemplate redisTemplate;

    private static Logger logger = LoggerFactory.getLogger(MessageSyncService.class);

    /**
     * @description 消息已读，更新会话已读的seq，并且通知在线的其他端。
     * @author chackylee
     * @date 2022/8/16 14:35
     * @param [messageReaded]
     * @return void
    */
    public void readMark(MessageReadedContent messageReaded) {
        shareThreadPool.submit(() -> {
            conversationService.messageMarkRead(messageReaded);
            MessageReadedAck ack = new MessageReadedAck();
            BeanUtils.copyProperties(messageReaded, ack);
            ack(messageReaded, ack, messageReaded.getFromId());
//            //同步给其他端

            syncToSender(messageReaded);

            //TODO 告诉对方已读

        });
    }

    /**
     * @description: 消息接收方ack，待考虑多个client的情况。如果使用服务端重传机制，可以不做此处理。
     * @param
     * @return void
     * @author lld
     * @since 2022/9/18
     */
    public void receiveMark(MessageReciveAckContent message) {

        shareThreadPool.submit(() -> {
//            conversationService.messageMarkRead(messageReaded);
//            MessageReadedAck ack = new MessageReadedAck();
//            BeanUtils.copyProperties(messageReaded, ack);
//            ack(messageReaded, ack, messageReaded.getFromId());
////            //同步给其他端
//
//            syncToSender(messageReaded);
            messageProducer.sendToUser(message.getToId(),MessageCommand.MSG_READED_ACK,message,message.getAppId());
        });
    }

    private void ack(ClientInfo clientInfo, MessageReadedAck readAck, String fromId) {
        ResponseVO<Object> wrappedResp = ResponseVO.successResponse(readAck);
        messageProducer.sendToUserAppointedClient(fromId, MessageCommand.MSG_READED_ACK, wrappedResp,
                new ClientInfo(clientInfo.getAppId(),clientInfo.getClientType(),clientInfo.getImei()));
    }

    private void syncToSender(MessageReadedContent messageReaded) {
        MessageReadedPack pack = new MessageReadedPack();
        BeanUtils.copyProperties(messageReaded,pack);
        ClientInfo clinetInfo = new ClientInfo(messageReaded.getAppId(), messageReaded.getClientType(), messageReaded.getImei());
        messageProducer.sendToUserExceptClient(messageReaded.getFromId(), MessageCommand.MSG_READED_NOTIFY, messageReaded
                , clinetInfo);
    }

    public ResponseVO syncOfflineMessage(SyncReq req){

        SyncResp<OfflineMessageContent> resp = new SyncResp<>();

        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        String key = req.getAppId() + ":" + Constants.RedisConstants.offlineMessage + ":" + req.getOperater();
        Set set = zSetOperations.reverseRange(key, 0, 0);
        Long maxSeq = 0L;
        if(!CollectionUtil.isEmpty(set)){
            List list=new ArrayList(set);
            Object o = list.get(0);
            OfflineMessageContent max = JSONObject.parseObject(o.toString(), OfflineMessageContent.class);
            maxSeq = max.getMessageSequence();
        }

        List<OfflineMessageContent> respList = new ArrayList<>();

        resp.setMaxSequence(maxSeq);
        Set<ZSetOperations.TypedTuple> set1 = zSetOperations.rangeByScoreWithScores(key, req.getLastSequence(), maxSeq, 0, req.getMaxLimit());
        for (ZSetOperations.TypedTuple typedTuple : set1) {
            Object value = typedTuple.getValue();
            respList.add(JSONObject.parseObject(value.toString(), OfflineMessageContent.class));
        }
        resp.setDataList(respList);

        if(CollectionUtil.isNotEmpty(respList)){
            OfflineMessageContent offlineMessageContent = respList.get(respList.size() - 1);
            resp.setCompleted(maxSeq >= offlineMessageContent.getMessageSequence());
        }

        return ResponseVO.successResponse(resp);
    }

}
