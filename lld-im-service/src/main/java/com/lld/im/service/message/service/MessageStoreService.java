package com.lld.im.service.message.service;

import cn.hutool.core.util.RandomUtil;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.DelFlagEnum;
import com.lld.im.common.enums.SyncFromEnum;
import com.lld.im.common.model.msg.*;
import com.lld.im.service.message.dao.ImMessageBodyEntity;
import com.lld.im.service.message.dao.ImMessageHistoryEntity;
import com.lld.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.lld.im.service.message.dao.mapper.ImMessageHistoryMapper;
import com.lld.im.service.service.seq.Seq;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @description: 消息存储服务
 * @author: lld
 * @createDate: 2022/7/23
 * @version: 1.0
 */
@Service
public class MessageStoreService {

    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    @Qualifier("snowflakeSeq")
    Seq seq;


    /**
     * @param
     * @return void
     * @description: 消息持久化。插入messageHistory和messageBody库
     * @author lld
     * @since 2022/7/23
     */
    public String storeGroupMessage(GroupChatMessageContent chatMessageContent) {
        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(chatMessageContent);
        imMessageBodyMapper.insert(imMessageBodyEntity);
        ImMessageHistoryEntity imMessageHistoryEntity = extractToGroupMessageHistory(chatMessageContent, imMessageBodyEntity);
        imMessageHistoryMapper.insert(imMessageHistoryEntity);
        return imMessageBodyEntity.getMessageKey();
    }

    /**
     * @param
     * @return void
     * @description: 消息持久化。插入messageHistory和messageBody库
     * @author lld
     * @since 2022/7/23
     */
    public String storeP2PMessage(ChatMessageContent chatMessageContent) {
        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(chatMessageContent);
        imMessageBodyMapper.insert(imMessageBodyEntity);
        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractToP2PMessageHistory(chatMessageContent,imMessageBodyEntity);
        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);
        return imMessageBodyEntity.getMessageKey();
    }

    /**
     * @param
     * @return void
     * @description: 消息持久化。插入messageHistory和messageBody库
     * @author lld
     * @since 2022/7/23
     */
    public void storeOffLineMessage(P2PMessageContent chatMessageContent) {

    }

    public List<ImMessageHistoryEntity> extractToP2PMessageHistory(ChatMessageContent content,ImMessageBodyEntity imMessageBodyEntity) {
        List<ImMessageHistoryEntity> list = new ArrayList<>();
        // 1 2 存2份
        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(content, fromHistory);
        fromHistory.setOwnerId(content.getFromId());
        long seq = this.seq.getSeq(content.getAppId() + Constants.SeqConstants.Message);
        fromHistory.setMessageHistroyId(seq);
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setDelFlag(DelFlagEnum.NORMAL.getCode());
        fromHistory.setSequence(content.getMessageSequence());
        fromHistory.setCreateTime(System.currentTimeMillis());
        list.add(fromHistory);
        if (content.getSyncFromId() == SyncFromEnum.BOTH.getCode()) {
            ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
            BeanUtils.copyProperties(content, toHistory);
            toHistory.setOwnerId(content.getToId());
//            long seq2 = this.seq.getSeq(content.getAppId() + Constants.SeqConstants.Message);
            toHistory.setMessageHistroyId(seq);
            toHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
            toHistory.setSequence(content.getMessageSequence());
            toHistory.setDelFlag(DelFlagEnum.NORMAL.getCode());
            toHistory.setCreateTime(System.currentTimeMillis());
            list.add(toHistory);
        }

        return list;
    }

    public ImMessageHistoryEntity extractToGroupMessageHistory(GroupChatMessageContent content, ImMessageBodyEntity imMessageBodyEntity) {
        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(content, fromHistory);
        fromHistory.setOwnerId(content.getFromId());
        long seq = this.seq.getSeq(content.getAppId() + Constants.SeqConstants.Message);
        fromHistory.setMessageHistroyId(seq);
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setDelFlag(DelFlagEnum.NORMAL.getCode());
        fromHistory.setSequence(content.getMessageSequence());
        fromHistory.setCreateTime(System.currentTimeMillis());

        return fromHistory;
    }


    public ImMessageBodyEntity extractMessageBody(MessageContent content) {

        ImMessageBodyEntity body = new ImMessageBodyEntity();
        body.setAppId(content.getAppId());
        body.setCreateTime(System.currentTimeMillis());
        body.setMessageBody(content.getMessageBody());
        body.setMessageKey(RandomUtil.randomString(16));
        body.setSecurityKey("");
        body.setMessageTime(content.getMessageTime());
        return body;
    }
}
