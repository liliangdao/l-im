package com.lld.im.service.conversation.service;

import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lld.im.codec.pack.MessageReadedPack;
import com.lld.im.common.constant.Constants;
import com.lld.im.service.conversation.dao.ImConversationSetEntity;
import com.lld.im.service.conversation.dao.mapper.ImConversationSetMapper;
import com.lld.im.service.service.seq.Seq;
import com.lld.im.service.utils.WriteUserSeq;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-29 16:20
 **/
@Service
public class ConversationService extends ServiceImpl<ImConversationSetMapper, ImConversationSetEntity> {

    @Autowired
    ImConversationSetMapper imConversationSetMapper;

    @Autowired
    @Qualifier("redisSeq")
    Seq seq;

    @Autowired
    WriteUserSeq writeUserSeq;

    public String convertConversationId(int type, String fromId, String toId) {
        //弄成util类抽出来
        return type + "_" + fromId + "_" + toId;
    }

    @Transactional
    public void msgMarkRead(MessageReadedPack messageReaded) {
        String conversationId = convertConversationId(messageReaded.getConversationType(), messageReaded.getFromId(), messageReaded.getToId());
        long seq = this.seq.getSeq(messageReaded.getAppId() + Constants.SeqConstants.Conversation);
        ImConversationSetEntity conversationSet = new ImConversationSetEntity();
        conversationSet.setConversationId(conversationId);
        BeanUtils.copyProperties(messageReaded,conversationSet);
        conversationSet.setConversationSequence(seq);
        imConversationSetMapper.markConversation(conversationSet);
//        cacheManager.refreshUserSyncSeqCache(messageReaded.getFromId(), SyncKeyEnum.syncConversationSetSequence.name(), String.valueOf(conversationSequence),messageReaded.getAppId());
        writeUserSeq.writeUserSeq(messageReaded.getAppId(),messageReaded.getFromId(),Constants.SeqConstants.Conversation,seq);
    }

}
