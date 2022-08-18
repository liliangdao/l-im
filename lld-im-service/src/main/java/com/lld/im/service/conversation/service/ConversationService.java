package com.lld.im.service.conversation.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lld.im.codec.pack.MessageReadedPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.model.SyncJoinedResp;
import com.lld.im.common.model.SyncReq;
import com.lld.im.service.conversation.dao.ImConversationSetEntity;
import com.lld.im.service.conversation.dao.mapper.ImConversationSetMapper;
import com.lld.im.service.group.dao.ImGroupEntity;
import com.lld.im.service.service.seq.Seq;
import com.lld.im.service.utils.WriteUserSeq;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public ResponseVO syncConversationSet(SyncReq req){

        if(req.getMaxLimit() > 100){
            req.setMaxLimit(100);
        }

        SyncJoinedResp resp = new SyncJoinedResp();

        QueryWrapper<ImConversationSetEntity> query = new QueryWrapper<>();
        query.eq("owner_id",req.getOperater());
        query.gt("conversation_sequence",req.getLastSequence());
        query.last(" limit " + req.getMaxLimit());
        List<ImConversationSetEntity> imConversationSetEntities = imConversationSetMapper.selectList(query);
//        List<ImGroupEntity> imGroupEntities = imGroupDataMapper.selectList(query);
        if(!CollectionUtil.isEmpty(imConversationSetEntities)){
            ImConversationSetEntity imGroupEntity = imConversationSetEntities.get(imConversationSetEntities.size()-1);
            Long seq = imConversationSetMapper.geConversationSerMaxSeq(req.getAppId(),req.getOperater());
            resp.setCompleted(imGroupEntity.getConversationSequence() >= seq);
            resp.setDataList(imConversationSetEntities);
            return ResponseVO.successResponse(resp);
        }

        return ResponseVO.successResponse();
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
