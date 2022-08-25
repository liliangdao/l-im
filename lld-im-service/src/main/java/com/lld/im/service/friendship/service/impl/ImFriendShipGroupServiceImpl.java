package com.lld.im.service.friendship.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.jeffreyning.mybatisplus.service.MppServiceImpl;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.DelFlagEnum;
import com.lld.im.common.enums.FriendShipErrorCode;
import com.lld.im.service.friendship.dao.ImFriendShipEntity;
import com.lld.im.service.friendship.dao.ImFriendShipGroupEntity;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipGroupMapper;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipGroupMemberMapper;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipMapper;
import com.lld.im.service.friendship.model.req.AddFriendShipGroupMemberReq;
import com.lld.im.service.friendship.model.req.AddFriendShipGroupReq;
import com.lld.im.service.friendship.model.req.DeleteFriendShipGroupReq;
import com.lld.im.service.friendship.service.ImFriendShipGroupMemberService;
import com.lld.im.service.friendship.service.ImFriendShipGroupService;
import com.lld.im.service.service.seq.Seq;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.utils.WriteUserSeq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-08-25 11:01
 **/
@Service
public class ImFriendShipGroupServiceImpl extends MppServiceImpl<ImFriendShipGroupMapper, ImFriendShipGroupEntity> implements ImFriendShipGroupService {

    @Autowired
    ImFriendShipGroupMapper imFriendShipGroupMapper;

    @Autowired
    ImFriendShipGroupMemberService imFriendShipGroupMemberService;

    @Autowired
    @Qualifier("redisSeq")
    Seq redisSeq;

    @Autowired
    @Qualifier("snowflakeSeq")
    Seq snowflakeSeq;

    @Autowired
    ImUserService imUserService;

    @Autowired
    WriteUserSeq writeUserSeq;

    @Override
    @Transactional
    public ResponseVO addGroup(AddFriendShipGroupReq req) {

        QueryWrapper<ImFriendShipGroupEntity> query = new QueryWrapper<>();
        query.eq("group_name", req.getGroupName());
        query.eq("app_id",req.getAppId());
        query.eq("from_id",req.getFromId());

        ImFriendShipGroupEntity entity = imFriendShipGroupMapper.selectOne(query);

        if(entity != null && entity.getDelFlag() != DelFlagEnum.NORMAL.getCode()){
            return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_EXIST);
        }

        //写入db
        ImFriendShipGroupEntity insert = new ImFriendShipGroupEntity();
        if(entity != null){
            insert.setGroupId(entity.getGroupId());
        }
        insert.setAppId(req.getAppId());
        insert.setCreateTime(System.currentTimeMillis());
        insert.setGroupName(req.getGroupName());
        long seq = redisSeq.getSeq(req.getAppId() + ":" + Constants.SeqConstants.FriendshipGroup);
        insert.setSequence(seq);
        insert.setFromId(req.getFromId());
        insert.setDelFlag(DelFlagEnum.NORMAL.getCode());
        try {
            boolean b = this.saveOrUpdate(insert);
            if(b){
                if(CollectionUtil.isNotEmpty(req.getToIds())){
                    AddFriendShipGroupMemberReq addFriendShipGroupMemberReq = new AddFriendShipGroupMemberReq();
                    addFriendShipGroupMemberReq.setFromId(req.getFromId());
                    addFriendShipGroupMemberReq.setGroupName(req.getGroupName());
                    addFriendShipGroupMemberReq.setToIds(req.getToIds());
                    addFriendShipGroupMemberReq.setAppId(req.getAppId());
                    addFriendShipGroupMemberReq.setClientType(req.getClientType());
                    addFriendShipGroupMemberReq.setImel(req.getImel());
                    imFriendShipGroupMemberService.addGroupMember(addFriendShipGroupMemberReq);
//                    req.getToIds().forEach(e ->{
//                        ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(e, req.getAppId());
//                        if(singleUserInfo.isOk()){
//                            imFriendShipGroupMemberService.doAddGroupMember(insert.getGroupId(),e);
//                        }
//                    });
                }
                return ResponseVO.successResponse();
            }
        }catch (DuplicateKeyException e){
            return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_EXIST);
        }
        //写入seq
        writeUserSeq.writeUserSeq(req.getAppId(),req.getFromId(),Constants.SeqConstants.FriendshipGroup,seq);

        return ResponseVO.errorResponse();
    }

    @Override
    @Transactional
    public ResponseVO deleteGroup(DeleteFriendShipGroupReq req) {

        QueryWrapper<ImFriendShipGroupEntity> query = new QueryWrapper<>();
        query.eq("group_name", req.getGroupName());
        query.eq("app_id",req.getAppId());
        query.eq("from_id",req.getFromId());

        ImFriendShipGroupEntity entity = imFriendShipGroupMapper.selectOne(query);
        if(entity != null){
            entity.setDelFlag(DelFlagEnum.DELETE.getCode());
            imFriendShipGroupMapper.updateById(entity);
            imFriendShipGroupMemberService.clearGroupMember(entity.getGroupId());
        }
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getGroup(String fromId, String groupName, Integer appId) {
        QueryWrapper<ImFriendShipGroupEntity> query = new QueryWrapper<>();
        query.eq("group_name", groupName);
        query.eq("app_id",appId);
        query.eq("from_id",fromId);

        ImFriendShipGroupEntity entity = imFriendShipGroupMapper.selectOne(query);
        if(entity == null){
            return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_SHIP_GROUP_IS_NOT_EXIST);
        }
        return ResponseVO.successResponse(entity);
    }
}
