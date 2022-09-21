package com.lld.im.service.message.service;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ConversationTypeEnum;
import com.lld.im.common.enums.DelFlagEnum;
import com.lld.im.common.enums.SyncFromEnum;
import com.lld.im.common.model.msg.*;
import com.lld.im.service.conversation.service.ConversationService;
import com.lld.im.service.group.dao.ImGroupMessageHistoryEntity;
import com.lld.im.service.group.dao.mapper.ImGroupMessageHistoryMapper;
import com.lld.im.service.message.dao.ImMessageBodyEntity;
import com.lld.im.service.message.dao.ImMessageHistoryEntity;
import com.lld.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.lld.im.service.message.dao.mapper.ImMessageHistoryMapper;
import com.lld.im.service.service.seq.Seq;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ConversationService conversationService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ImGroupMessageHistoryMapper imGroupMessageHistoryMapper;


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
        ImGroupMessageHistoryEntity imGroupMessageHistoryEntity = extractToGroupMessageHistory(chatMessageContent, imMessageBodyEntity);
        extractToGroupMessageHistory(chatMessageContent, imMessageBodyEntity);
        imGroupMessageHistoryMapper.insert(imGroupMessageHistoryEntity);
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
     * @description: 根据客户端messageId从最近的缓存中添加消息
     * @param
     * @return java.lang.String
     * @author lld
     * @since 2022/9/18
     */
    public void setMessageFromMessageIdCache(P2PMessageContent p2PMessageContent) {
        String key = p2PMessageContent.getAppId() + ":" + Constants.RedisConstants.cacheMessage + ":" + p2PMessageContent.getMessageId();
        stringRedisTemplate.opsForValue().set(key,JSONObject.toJSONString(p2PMessageContent),300, TimeUnit.SECONDS);
    }

    /**
     * @description: 根据客户端messageId从最近的缓存中获取消息,如果存在直接重新发送。如果不存在才走正常发消息逻辑
     * @param
     * @return java.lang.String
     * @author lld
     * @since 2022/9/18
     */
    public P2PMessageContent getMessageFromMessageIdCache(String messageId,Integer appId) {
        String msg = stringRedisTemplate.opsForValue().get(appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId);
        if(StringUtils.isBlank(msg)){
            return null;
        }
        P2PMessageContent p2PMessageContent = JSONObject.parseObject(msg, P2PMessageContent.class);
        return p2PMessageContent;
    }

    /**
     * @description 存储离线消息到redis，发送方和接收方都要存一份
     * @author chackylee
     * @date 2022/8/19 15:30
     * @param [chatMessageContent]
     * @return void
    */
    public void storeOffLineMessage(OfflineMessageContent offlineMessageContent) {

        offlineMessageContent.setConversationId(conversationService.convertConversationId(offlineMessageContent.getConversationType(),
                offlineMessageContent.getFromId(),offlineMessageContent.getToId()));
        offlineMessageContent.setConversationType(offlineMessageContent.getConversationType());
        offlineMessageContent.setMessageKey(offlineMessageContent.getMessageKey());

        ZSetOperations zSetOperations = redisTemplate.opsForZSet();

        String fromKey = offlineMessageContent.getAppId() + ":" + Constants.RedisConstants.offlineMessage + ":" + offlineMessageContent.getFromId();
        String toKey = offlineMessageContent.getAppId() + ":" + Constants.RedisConstants.offlineMessage + ":" + offlineMessageContent.getToId();

        //给发送方插入离线消息
        Long fromCount = zSetOperations.zCard(fromKey);

        if(fromCount > appConfig.getOfflineMessageCount()){
            zSetOperations.removeRange(fromKey,0,0);
        }

        zSetOperations.add(fromKey, JSONObject.toJSONString(offlineMessageContent),offlineMessageContent.getMessageSequence());
        //给接收方插入离线消息
        if(offlineMessageContent.getConversationType() != ConversationTypeEnum.GROUP.getCode()){
            Long toCount = zSetOperations.zCard(toKey);

            if(toCount > appConfig.getOfflineMessageCount()){
                zSetOperations.removeRange(toKey,0,0);
            }
            zSetOperations.add(toKey,JSONObject.toJSONString(offlineMessageContent),offlineMessageContent.getMessageSequence());

        }
    }


    public List<ImMessageHistoryEntity> extractToP2PMessageHistory(ChatMessageContent content,ImMessageBodyEntity imMessageBodyEntity) {
        List<ImMessageHistoryEntity> list = new ArrayList<>();
        // 1 2 存2份
        ImMessageHistoryEntity fromHistory = new ImMessageHistoryEntity();
        BeanUtils.copyProperties(content, fromHistory);
        fromHistory.setOwnerId(content.getFromId());
        long fromMessageHistoryId = this.seq.getSeq("");
        fromHistory.setMessageHistroyId(fromMessageHistoryId);
        fromHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
        fromHistory.setDelFlag(DelFlagEnum.NORMAL.getCode());
        fromHistory.setSequence(content.getMessageSequence());
        fromHistory.setCreateTime(System.currentTimeMillis());
        list.add(fromHistory);
        if (content.getSyncFromId() == SyncFromEnum.BOTH.getCode()) {
            ImMessageHistoryEntity toHistory = new ImMessageHistoryEntity();
            BeanUtils.copyProperties(content, toHistory);
            toHistory.setOwnerId(content.getToId());
            long toMessageHistoryId = this.seq.getSeq("");
//            long seq2 = this.seq.getSeq(content.getAppId() + Constants.SeqConstants.Message);
            toHistory.setMessageHistroyId(toMessageHistoryId);
            toHistory.setMessageKey(imMessageBodyEntity.getMessageKey());
            toHistory.setSequence(content.getMessageSequence());
            toHistory.setDelFlag(DelFlagEnum.NORMAL.getCode());
            toHistory.setCreateTime(System.currentTimeMillis());
            list.add(toHistory);
        }

        return list;
    }

    public ImGroupMessageHistoryEntity extractToGroupMessageHistory(GroupChatMessageContent content, ImMessageBodyEntity imMessageBodyEntity) {
        ImGroupMessageHistoryEntity fromHistory = new ImGroupMessageHistoryEntity();
        BeanUtils.copyProperties(content, fromHistory);
        fromHistory.setGroupId(content.getGroupId());
        long seq = this.seq.getSeq("");
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
