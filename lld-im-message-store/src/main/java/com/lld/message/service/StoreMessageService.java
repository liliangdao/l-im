package com.lld.message.service;

import com.lld.im.common.enums.SyncFromEnum;
import com.lld.im.common.model.msg.ChatMessageContent;
import com.lld.im.common.model.msg.GroupChatMessageContent;
import com.lld.message.dao.ImGroupMessageHistoryEntity;
import com.lld.message.dao.ImMessageBodyEntity;
import com.lld.message.dao.ImMessageHistoryEntity;
import com.lld.message.dao.mapper.ImGroupMessageHistoryMapper;
import com.lld.message.dao.mapper.ImMessageBodyMapper;
import com.lld.message.dao.mapper.ImMessageHistoryMapper;
import com.lld.message.model.DoStroeGroupMessageDto;
import com.lld.message.model.DoStroeP2PMessageDto;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-11-29 14:57
 **/
@Component
public class StoreMessageService {

    @Autowired
    ImMessageBodyMapper imMessageBodyMapper;

    @Autowired
    ImMessageHistoryMapper imMessageHistoryMapper;

    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;

    /**
     * @param
     * @return void
     * @description: 消息持久化。
     * @author lld
     */
    public void doStoreP2PMessage(DoStroeP2PMessageDto chatMessageContent) {
        imMessageBodyMapper.insert(chatMessageContent.getImMessageBodyEntity());
        List<ImMessageHistoryEntity> imMessageHistoryEntities
                = extractToP2PMessageHistory(chatMessageContent.getChatMessageContent(), chatMessageContent.getImMessageBodyEntity());
        imMessageHistoryMapper.insertBatchSomeColumn(imMessageHistoryEntities);
    }

    /**
     * @param
     * @return void
     * @description: 群聊消息持久化
     * @author lld
     */
    public void doStoreGroupMessage(DoStroeGroupMessageDto chatMessageContent) {
        imMessageBodyMapper.insert(chatMessageContent.getImMessageBodyEntity());
        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(chatMessageContent.getChatMessageContent(), chatMessageContent.getImMessageBodyEntity());
        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);
    }

    public List<ImMessageHistoryEntity> extractToP2PMessageHistory(ChatMessageContent content, ImMessageBodyEntity imMessageBodyEntity) {
        List<ImMessageHistoryEntity> list = new ArrayList<>();
        // 1 2 存2份
        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(content, fromHistory);
        fromHistory.setOwnerId(content.getFromId());
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setSequence(content.getMessageSequence());
        fromHistory.setCreateTime(System.currentTimeMillis());
        list.add(fromHistory);
        if (content.getSyncFromId() == SyncFromEnum.BOTH.getCode()) {
            ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
            BeanUtils.copyProperties(content, toHistory);
            toHistory.setOwnerId(content.getToId());
            toHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
            toHistory.setSequence(content.getMessageSequence());
            toHistory.setCreateTime(System.currentTimeMillis());
            list.add(toHistory);
        }

        return list;
    }

    public ImGroupMessageHistoryEntity extractToGroupMessageHistory(GroupChatMessageContent content, ImMessageBodyEntity imMessageBodyEntity) {
        ImGroupMessageHistoryEntity fromHistory = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(content, fromHistory);
        fromHistory.setGroupId(content.getGroupId());
//        long seq = this.seq.getSeq("");
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setSequence(content.getMessageSequence());
        fromHistory.setCreateTime(System.currentTimeMillis());

        return fromHistory;
    }
}
