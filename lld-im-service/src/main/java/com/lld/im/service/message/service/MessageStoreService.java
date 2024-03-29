package com.lld.im.service.message.service;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ConversationTypeEnum;
import com.lld.im.common.enums.DelFlagEnum;
import com.lld.im.common.enums.SyncFromEnum;
import com.lld.im.common.model.msg.*;
import com.lld.im.common.model.msg.dto.DoStroeGroupMessageDto;
import com.lld.im.common.model.msg.dto.DoStroeP2PMessageDto;
import com.lld.im.common.model.msg.dto.ImMessageBody;
import com.lld.im.service.conversation.service.ConversationService;
import com.lld.im.service.group.dao.ImGroupMessageHistoryEntity;
import com.lld.im.service.group.dao.mapper.ImGroupMessageHistoryMapper;
import com.lld.im.service.group.service.ImGroupMemberService;
import com.lld.im.service.message.dao.ImMessageBodyEntity;
import com.lld.im.service.message.dao.ImMessageHistoryEntity;
import com.lld.im.service.message.dao.mapper.ImMessageBodyMapper;
import com.lld.im.service.message.dao.mapper.ImMessageHistoryMapper;
import com.lld.im.service.service.seq.Seq;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    @Autowired
    ImGroupMemberService groupMemberService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * @param
     * @return void
     * @description: 消息持久化。插入messageHistory和messageBody库
     * @author lld
     * @since 2022/7/23
     */
    public void storeGroupMessage(GroupChatMessageContent chatMessageContent) {
        ImMessageBody imMessageBody = extractMessageBody(chatMessageContent);
        DoStroeGroupMessageDto doStroeGroupMessageDto = new DoStroeGroupMessageDto();
        doStroeGroupMessageDto.setImMessageBody(imMessageBody);
        doStroeGroupMessageDto.setChatMessageContent(chatMessageContent);
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.StoreGroupMessage,"",
                JSONObject.toJSONString(doStroeGroupMessageDto));
        chatMessageContent.setMessageKey(imMessageBody.getMessageKey());
    }

    /**
     * @param
     * @return void
     * @description: 消息持久化。插入messageHistory和messageBody库
     * @author lld
     * @since 2022/7/23
     */
    public void storeP2PMessage(ChatMessageContent chatMessageContent) {
        ImMessageBody imMessageBodyEntity = extractMessageBody(chatMessageContent);
        DoStroeP2PMessageDto doStroeP2PMessageDto = new DoStroeP2PMessageDto();
        doStroeP2PMessageDto.setChatMessageContent(chatMessageContent);
        doStroeP2PMessageDto.setImMessageBody(imMessageBodyEntity);
        rabbitTemplate.convertAndSend(Constants.RabbitConstants.StoreP2PMessage,"",
                JSONObject.toJSONString(doStroeP2PMessageDto));
        chatMessageContent.setMessageKey(imMessageBodyEntity.getMessageKey());
    }

    /**
     * @param
     * @return java.lang.String
     * @description: 根据客户端messageId从最近的缓存中添加消息
     * @author lld
     * @since 2022/9/18
     */
    public void setMessageFromMessageIdCache(MessageContent p2PMessageContent) {
        String key = p2PMessageContent.getAppId() + ":" + Constants.RedisConstants.cacheMessage + ":" + p2PMessageContent.getMessageId();
        stringRedisTemplate.opsForValue().set(key, JSONObject.toJSONString(p2PMessageContent), 300, TimeUnit.SECONDS);
    }

    /**
     * @param
     * @return java.lang.String
     * @description: 根据客户端messageId从最近的缓存中获取消息, 如果存在直接重新发送。如果不存在才走正常发消息逻辑
     * @author lld
     * @since 2022/9/18
     */
    public MessageContent getMessageFromMessageIdCache(String messageId, Integer appId) {
        String msg = stringRedisTemplate.opsForValue().get(appId + ":" + Constants.RedisConstants.cacheMessage + ":" + messageId);
        if (StringUtils.isBlank(msg)) {
            return null;
        }
        MessageContent p2PMessageContent = JSONObject.parseObject(msg, MessageContent.class);
        return p2PMessageContent;
    }

    /**
     * @param
     * @return void
     * @description 存储离线消息到redis，发送方和接收方都要存一份
     * @author chackylee
     * @date 2022/8/19 15:30
     */
    public void storeOffLineMessage(OfflineMessageContent offlineMessageContent) {

        offlineMessageContent.setConversationId(conversationService.convertConversationId(ConversationTypeEnum.P2P.getCode(),
                offlineMessageContent.getFromId(), offlineMessageContent.getToId()));

        ZSetOperations zSetOperations = redisTemplate.opsForZSet();

        String fromKey = offlineMessageContent.getAppId() + ":" + Constants.RedisConstants.offlineMessage + ":" + offlineMessageContent.getFromId();

        //给发送方插入离线消息
        Long fromCount = zSetOperations.zCard(fromKey);

        if (fromCount > appConfig.getOfflineMessageCount()) {
            zSetOperations.removeRange(fromKey, 0, 0);
        }

        zSetOperations.add(fromKey, JSONObject.toJSONString(offlineMessageContent), offlineMessageContent.getMessageKey());
        //给接收方插入离线消息
        String toKey = offlineMessageContent.getAppId() + ":" +
                Constants.RedisConstants.offlineMessage + ":" +
                offlineMessageContent.getToId();

        Long toCount = zSetOperations.zCard(toKey);

        if (toCount > appConfig.getOfflineMessageCount()) {
            zSetOperations.removeRange(toKey, 0, 0);
        }
        zSetOperations.add(toKey, JSONObject.toJSONString(offlineMessageContent), offlineMessageContent.getMessageKey());

    }

    public void storeGroupOffLineMessage(OfflineMessageContent offlineMessageContent,List<String> memberId) {

        offlineMessageContent.setConversationId(conversationService.convertConversationId(ConversationTypeEnum.GROUP.getCode(),
                offlineMessageContent.getFromId(), offlineMessageContent.getToId()));

        ZSetOperations zSetOperations = redisTemplate.opsForZSet();

        String fromKey = offlineMessageContent.getAppId() + ":" + Constants.RedisConstants.offlineMessage + ":" + offlineMessageContent.getFromId();

        //给发送方插入离线消息
        Long fromCount = zSetOperations.zCard(fromKey);

        if (fromCount > appConfig.getOfflineMessageCount()) {
            zSetOperations.removeRange(fromKey, 0, 0);
        }

        zSetOperations.add(fromKey, JSONObject.toJSONString(offlineMessageContent), offlineMessageContent.getMessageKey());
        //给接收方插入离线消息
        for (String member : memberId) {
            String toKey = offlineMessageContent.getAppId() + ":" + Constants.RedisConstants.offlineMessage + ":" + member;
            Long toCount = zSetOperations.zCard(toKey);
            if (toCount > appConfig.getOfflineMessageCount()) {
                zSetOperations.removeRange(toKey, 0, 0);
            }
            zSetOperations.add(toKey, JSONObject.toJSONString(offlineMessageContent), offlineMessageContent.getMessageKey());
        }
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

    public ImMessageBody extractMessageBody(MessageContent content) {

        ImMessageBody body = new ImMessageBody();
        body.setAppId(content.getAppId());
        body.setCreateTime(System.currentTimeMillis());
        body.setMessageBody(content.getMessageBody());
        body.setMessageKey(seq.getSeq(""));
        body.setSecurityKey("");
        body.setMessageTime(System.currentTimeMillis());
        body.setDelFlag(DelFlagEnum.NORMAL.getCode());
        return body;
    }
}
