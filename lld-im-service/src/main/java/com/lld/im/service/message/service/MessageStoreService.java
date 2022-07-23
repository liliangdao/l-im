package com.lld.im.service.message.service;

import com.lld.im.common.enums.SyncFromEnum;
import com.lld.im.common.model.msg.ChatMessageContent;
import com.lld.im.service.message.dao.ImMessageBodyEntity;
import com.lld.im.service.message.dao.ImMessageHistoryEntity;
import com.lld.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.lld.im.service.message.dao.mapper.ImMessageHistoryMapper;
import com.lld.im.service.service.seq.Seq;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
    public void storeMessage(ChatMessageContent chatMessageContent) {

        ImMessageBodyEntity imMessageBodyEntity = extractMessageBody(chatMessageContent);
        imMessageBodyMapper.insert(imMessageBodyEntity);
        List<ImMessageHistoryEntity> imMessageHistoryEntities = extractToMessageHistory(chatMessageContent);
    }

    public List<ImMessageHistoryEntity> extractToMessageHistory(ChatMessageContent content) {



        List<ImMessageHistoryEntity> list = new ArrayList<>();
        // 1 2 存2份

        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        list.add(fromHistory);
        BeanUtils.copyProperties(content, fromHistory);
//        if (content.getSyncFromId() == SyncFromEnum.BOTH.getCode()) {
//        }


        return null;
    }


    public ImMessageBodyEntity extractMessageBody(ChatMessageContent content) {

        return null;
    }
}
