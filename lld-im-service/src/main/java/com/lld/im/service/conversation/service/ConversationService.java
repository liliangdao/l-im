package com.lld.im.service.conversation.service;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lld.im.codec.pack.MessageReadedPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.model.SyncReq;
import com.lld.im.common.model.SyncResp;
import com.lld.im.common.model.msg.MessageReadedContent;
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

        SyncResp resp = new SyncResp();

        QueryWrapper<ImConversationSetEntity> query = new QueryWrapper<>();
        query.eq("from_id",req.getOperater());
        query.gt("sequence",req.getLastSequence());
        query.last(" limit " + req.getMaxLimit());
        query.orderByAsc("sequence");
        List<ImConversationSetEntity> imConversationSetEntities = imConversationSetMapper.selectList(query);
//        List<ImGroupEntity> imGroupEntities = imGroupDataMapper.selectList(query);
        if(!CollectionUtil.isEmpty(imConversationSetEntities)){
            ImConversationSetEntity imGroupEntity = imConversationSetEntities.get(imConversationSetEntities.size()-1);
            Long seq = imConversationSetMapper.geConversationSetMaxSeq(req.getAppId(),req.getOperater());
            resp.setCompleted(imGroupEntity.getSequence() >= seq);
            resp.setDataList(imConversationSetEntities);
            resp.setMaxSequence(seq);
            return ResponseVO.successResponse(resp);
        }

        return ResponseVO.successResponse();
    }


    @Transactional
    public void msgMarkRead(MessageReadedContent messageReaded) {
        String conversationId = convertConversationId(messageReaded.getConversationType(), messageReaded.getFromId(), messageReaded.getToId());
        long seq = this.seq.getSeq(messageReaded.getAppId() + ":" + Constants.SeqConstants.Conversation);
        ImConversationSetEntity conversationSet = new ImConversationSetEntity();
        conversationSet.setConversationId(conversationId);
        BeanUtils.copyProperties(messageReaded,conversationSet);
        conversationSet.setSequence(seq);
        conversationSet.setReadedSequence(messageReaded.getMessageSequence());
        imConversationSetMapper.markConversation(conversationSet);
//        cacheManager.refreshUserSyncSeqCache(messageReaded.getFromId(), SyncKeyEnum.syncConversationSetSequence.name(), String.valueOf(conversationSequence),messageReaded.getAppId());
        writeUserSeq.writeUserSeq(messageReaded.getAppId(),messageReaded.getFromId(),Constants.SeqConstants.Conversation,seq);
    }

}
