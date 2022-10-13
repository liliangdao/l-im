package com.lld.im.service.group.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.lld.im.codec.pack.CreateGroupPack;
import com.lld.im.codec.pack.DestroyGroupPack;
import com.lld.im.codec.pack.TransferGroupPack;
import com.lld.im.codec.pack.UpdateGroupInfoPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.*;
import com.lld.im.common.enums.command.GroupEventCommand;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.SyncReq;
import com.lld.im.common.model.SyncResp;
import com.lld.im.service.group.dao.ImGroupEntity;
import com.lld.im.service.group.dao.mapper.ImGroupMapper;
import com.lld.im.service.group.model.req.*;
import com.lld.im.service.group.model.resp.GetGroupResp;
import com.lld.im.service.group.model.resp.GetJoinedGroupResp;
import com.lld.im.service.group.model.resp.GetRoleInGroupResp;
import com.lld.im.service.group.service.GroupMessageProducer;
import com.lld.im.service.group.service.ImGroupMemberService;
import com.lld.im.service.group.service.ImGroupService;
import com.lld.im.service.service.seq.Seq;
import com.lld.im.service.utils.CallbackService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/9
 * @version: 1.0
 */
@Service
public class ImGroupServiceImpl implements ImGroupService {

    @Autowired
    ImGroupMapper imGroupDataMapper;

    @Autowired
    ImGroupMemberService groupMemberService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    @Qualifier("redisSeq")
    Seq seq;

    @Autowired
    CallbackService callbackService;

    @Autowired
    GroupMessageProducer groupMessageProducer;

    @Override
    public ResponseVO importGroup(ImportGroupReq req) {

        //1.判断群id是否存在
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();

        if (StringUtils.isEmpty(req.getGroupId())) {
            req.setGroupId(UUID.randomUUID().toString().replace("-", ""));
        } else {
            query.eq("group_id", req.getGroupId());
            query.eq("app_id", req.getAppId());
            Integer integer = imGroupDataMapper.selectCount(query);
            if (integer > 0) {
                throw new ApplicationException(GroupErrorCode.GROUP_IS_EXIST);
            }
        }

        if (req.getMaxMemberCount() != null && req.getMaxMemberCount() > appConfig.getGroupMaxMemberCount()) {
            throw new ApplicationException(GroupErrorCode.GROUP_MEMBER_IS_BEYOND);
        }

        ImGroupEntity imGroupEntity = new ImGroupEntity();

        if(req.getGroupType() == GroupTypeEnum.PUBLIC.getCode() && StringUtils.isBlank(req.getOwnerId())){
            throw new ApplicationException(GroupErrorCode.PUBLIC_GROUP_MUST_HAVE_OWNER);
        }

        if(req.getCreateTime() == null){
            imGroupEntity.setCreateTime(System.currentTimeMillis());
        }
        imGroupEntity.setStatus(GroupStatusEnum.NORMAL.getCode());
        long seq = this.seq.getSeq(req.getAppId() + ":" + Constants.SeqConstants.Group);
        imGroupEntity.setSequence(seq);
        BeanUtils.copyProperties(req, imGroupEntity);
        int insert = imGroupDataMapper.insert(imGroupEntity);

        if (insert != 1) {
            throw new ApplicationException(GroupErrorCode.IMPORT_GROUP_ERROR);
        }

        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO createGroup(CreateGroupReq req) {

        boolean isAdmin = false;

        if (!isAdmin) {
            req.setOwnerId(req.getOperater());
        }

        //1.判断群id是否存在
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();

        if (StringUtils.isEmpty(req.getGroupId())) {
            req.setGroupId(UUID.randomUUID().toString().replace("-", ""));
        } else {
            query.eq("group_id", req.getGroupId());
            query.eq("app_id", req.getAppId());
            Integer integer = imGroupDataMapper.selectCount(query);
            if (integer > 0) {
                throw new ApplicationException(GroupErrorCode.GROUP_IS_EXIST);
            }
        }

        if(req.getGroupType() == GroupTypeEnum.PUBLIC.getCode() && StringUtils.isBlank(req.getOwnerId())){
            throw new ApplicationException(GroupErrorCode.PUBLIC_GROUP_MUST_HAVE_OWNER);
        }

        if (req.getMaxMemberCount() != null && req.getMaxMemberCount() > appConfig.getGroupMaxMemberCount()) {
            throw new ApplicationException(GroupErrorCode.GROUP_MEMBER_IS_BEYOND);
        }

        if (req.getMember().size() > appConfig.getGroupMaxMemberCount()) {
            throw new ApplicationException(GroupErrorCode.GROUP_MEMBER_IS_BEYOND);
        }

        ImGroupEntity imGroupEntity = new ImGroupEntity();
        long seq = this.seq.getSeq(req.getAppId() + ":" + Constants.SeqConstants.Group);
        imGroupEntity.setSequence(seq);
        imGroupEntity.setCreateTime(System.currentTimeMillis());
        imGroupEntity.setStatus(GroupStatusEnum.NORMAL.getCode());
        BeanUtils.copyProperties(req, imGroupEntity);
        int insert = imGroupDataMapper.insert(imGroupEntity);

        GroupMemberDto groupMemberDto = new GroupMemberDto();
        groupMemberDto.setMemberId(req.getOwnerId());
        groupMemberDto.setRole(GroupMemberRoleEnum.OWNER.getCode());
        groupMemberDto.setJoinTime(System.currentTimeMillis());
        groupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), groupMemberDto);

        //插入群成员
        for (GroupMemberDto dto : req.getMember()) {
            groupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), dto);
        }

        CreateGroupPack createGroupPack = new CreateGroupPack();
        BeanUtils.copyProperties(imGroupEntity, createGroupPack);
        groupMessageProducer.producer(req.getOperater(), GroupEventCommand.CREATED_GROUP, createGroupPack
                , new ClientInfo(req.getAppId(), req.getClientType(), req.getImel()));

        //回调
        if (appConfig.isCreateGroupCallback()) {
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.CreateGroup, JSONObject.toJSONString(imGroupEntity));
        }
        return ResponseVO.successResponse();
    }

    /**
     * @param [req]
     * @return com.lld.im.common.ResponseVO
     * @description 修改群基础信息，如果是后台管理员调用，则不检查权限，如果不是则检查权限，如果是私有群（微信群）任何人都可以修改资料，公开群只有管理员可以修改
     * 如果是群主或者管理员可以修改其他信息。
     * @author chackylee
     * @date 2022/7/14 10:11
     */
    @Override
    @Transactional
    public ResponseVO updateBaseGroupInfo(UpdateGroupReq req) {

        //1.判断群id是否存在
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        query.eq("group_id", req.getGroupId());
        query.eq("app_id", req.getAppId());
        ImGroupEntity imGroupEntity = imGroupDataMapper.selectOne(query);
        if (imGroupEntity == null) {
            throw new ApplicationException(GroupErrorCode.GROUP_IS_EXIST);
        }

        boolean isAdmin = false;

        if (!isAdmin) {
            //不是后台调用需要检查权限
            ResponseVO<GetRoleInGroupResp> role = groupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOperater(), req.getAppId());

            if (!role.isOk()) {
                return role;
            }

            GetRoleInGroupResp data = role.getData();
            Integer roleInfo = data.getRole();

            boolean isManager = roleInfo == GroupMemberRoleEnum.MAMAGER.getCode() || roleInfo == GroupMemberRoleEnum.OWNER.getCode();

            //公开群只能群主修改资料
            if (!isManager && GroupTypeEnum.PUBLIC.getCode() == imGroupEntity.getGroupType()) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }

//            if (StringUtils.isNotBlank(req.getIntroduction()) || StringUtils.isNotBlank(req.getGroupName()) ||
//                    StringUtils.isNotBlank(req.getNotification()) ||
//                    StringUtils.isNotBlank(req.getPhoto()) ||
//                    req.getMute() != null ||
//                    GroupMuteTypeEnum.getEnum(req.getMute()) != null) {
//                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
//            }
        }

        ImGroupEntity update = new ImGroupEntity();
        long seq = this.seq.getSeq(req.getAppId() + ":" + Constants.SeqConstants.Group);
        update.setSequence(seq);
        BeanUtils.copyProperties(req, update);
        update.setUpdateTime(System.currentTimeMillis());

        int row = imGroupDataMapper.update(update, query);
        if (row != 1) {
            throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
        }

        UpdateGroupInfoPack pack = new UpdateGroupInfoPack();
        BeanUtils.copyProperties(req, pack);

        groupMessageProducer.producer(req.getOperater(), GroupEventCommand.UPDATED_GROUP,
                pack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImel()));
        if (appConfig.isModifyGroupCallback()) {
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.UpdateGroup, JSONObject.toJSONString(imGroupDataMapper.selectOne(query)));

        }

        return ResponseVO.successResponse();
    }

    /**
     * @param [req]
     * @return com.lld.im.common.ResponseVO
     * @description 获取用户加入的群组
     * @author chackylee
     * @date 2022/7/14 11:45
     */
    @Override
    public ResponseVO getJoinedGroup(GetJoinedGroupReq req) {

        ResponseVO<Collection<String>> memberJoinedGroup = groupMemberService.getMemberJoinedGroup(req);
        if (memberJoinedGroup.isOk()) {

            GetJoinedGroupResp resp = new GetJoinedGroupResp();

            if (CollectionUtils.isEmpty(memberJoinedGroup.getData())) {
                resp.setTotalCount(0);
                resp.setGroupList(new ArrayList<>());
                return ResponseVO.successResponse(resp);
            }

            QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
            query.eq("app_id", req.getAppId());
            query.in("group_id", memberJoinedGroup.getData());

            if (CollectionUtils.isNotEmpty(req.getGroupType())) {
                query.in("group_type", req.getGroupType());
            }

            List<ImGroupEntity> groupList = imGroupDataMapper.selectList(query);
            resp.setGroupList(groupList);
            if (req.getLimit() == null) {
                resp.setTotalCount(groupList.size());
            } else {
                resp.setTotalCount(imGroupDataMapper.selectCount(query));
            }
            return ResponseVO.successResponse(resp);
        } else {
            return memberJoinedGroup;
        }
    }

    /**
     * @param [req]
     * @return com.lld.im.common.ResponseVO
     * @description 增量同步加入的群聊列表，传0拉所有
     * @author chackylee
     * @date 2022/8/18 10:01
     */
    @Override
    public ResponseVO syncJoinedGroupList(SyncReq req) {

        if (req.getMaxLimit() > 100) {
            req.setMaxLimit(100);
        }

        SyncResp resp = new SyncResp();

        ResponseVO<Collection<String>> memberJoinedGroup = groupMemberService.syncMemberJoinedGroup(req);
        if (memberJoinedGroup.isOk()) {

            Collection<String> data = memberJoinedGroup.getData();
            QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
            query.eq("app_id", req.getAppId());
            query.in("group_id", data);
            query.gt("sequence", req.getLastSequence());
            query.orderByAsc("sequence");
            query.last("limit " + req.getMaxLimit());

            List<ImGroupEntity> imGroupEntities = imGroupDataMapper.selectList(query);
            if (!CollectionUtil.isEmpty(imGroupEntities)) {
                ImGroupEntity imGroupEntity = imGroupEntities.get(imGroupEntities.size() - 1);
                Long memberJoinedGroupMaxSeq = imGroupDataMapper.getMemberJoinedGroupMaxSeq(req.getAppId(), data);
                resp.setCompleted(imGroupEntity.getSequence() >= memberJoinedGroupMaxSeq);
                resp.setDataList(imGroupEntities);
                resp.setMaxSequence(memberJoinedGroupMaxSeq);
                return ResponseVO.successResponse(resp);
            }
        }

        return memberJoinedGroup;
    }

    /**
     * @param [req]
     * @return com.lld.im.common.ResponseVO
     * @description 解散群组，只支持后台管理员和群主解散，需发送tcp通知 + 回调
     * @author chackylee
     * @date 2022/7/14 11:45
     */
    @Override
    @Transactional
    public ResponseVO destroyGroup(DestroyGroupReq req) {

        boolean isAdmin = false;

        QueryWrapper<ImGroupEntity> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("group_id", req.getGroupId());
        objectQueryWrapper.eq("app_id", req.getAppId());
        ImGroupEntity imGroupEntity = imGroupDataMapper.selectOne(objectQueryWrapper);
        if (imGroupEntity == null) {
            throw new ApplicationException(GroupErrorCode.PRIVATE_GROUP_CAN_NOT_DESTORY);
        }

        if (!isAdmin) {
            if(imGroupEntity.getGroupType() == GroupTypeEnum.PUBLIC.getCode()){
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
            }

            if (imGroupEntity.getGroupType() == GroupTypeEnum.PUBLIC.getCode() &&
                    !imGroupEntity.getOwnerId().equals(req.getOperater())) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
            }
        }

        ImGroupEntity update = new ImGroupEntity();
        update.setStatus(GroupStatusEnum.DESTROY.getCode());
        long seq = this.seq.getSeq(req.getAppId() + ":" + Constants.SeqConstants.Group);
        update.setSequence(seq);
        int update1 = imGroupDataMapper.update(update, objectQueryWrapper);
        if (update1 != 1) {
            throw new ApplicationException(GroupErrorCode.UPDATE_GROUP_BASE_INFO_ERROR);
        }

        DestroyGroupPack pack = new DestroyGroupPack();
        pack.setGroupId(req.getGroupId());
        groupMessageProducer.producer(req.getOperater(),
                GroupEventCommand.DESTROY_GROUP, pack, new ClientInfo(req.getAppId(), req.getClientType(), req.getImel()));

        if (appConfig.isDestroyGroupCallback()) {
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.DestoryGroup, JSONObject.toJSONString(imGroupDataMapper.selectOne(objectQueryWrapper)));
        }
        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO transferGroup(TransferGroupReq req) {

        ResponseVO<GetRoleInGroupResp> roleInGroupOne = groupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOperater(), req.getAppId());
        if(!roleInGroupOne.isOk()){
            return roleInGroupOne;
        }

        if(roleInGroupOne.getData().getRole() != GroupMemberRoleEnum.OWNER.getCode()){
            return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
        }

        ResponseVO<GetRoleInGroupResp> newOwnerRole = groupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOwnerId(), req.getAppId());
        if(!newOwnerRole.isOk()){
            return newOwnerRole;
        }

        ImGroupEntity updateGroup = new ImGroupEntity();
        updateGroup.setOwnerId(req.getOwnerId());
        UpdateWrapper<ImGroupEntity> updateGroupWrapper = new UpdateWrapper<>();
        updateGroupWrapper.eq("app_id",req.getAppId());
        updateGroupWrapper.eq("group_id",req.getGroupId());
        imGroupDataMapper.update(updateGroup,updateGroupWrapper);
        groupMemberService.transferGroupMember(req.getOwnerId(), req.getGroupId(), req.getAppId());

        TransferGroupPack pack = new TransferGroupPack();
        pack.setGroupId(req.getGroupId());
        pack.setOwnerId(req.getOwnerId());
        groupMessageProducer.producer(req.getOperater(),GroupEventCommand.TRANSFER_GROUP,pack,new ClientInfo(req.getAppId(),req.getClientType(),req.getImel()));

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getGroup(String groupId, Integer appId) {

        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId);
        query.eq("group_id", groupId);
        ImGroupEntity imGroupEntity = imGroupDataMapper.selectOne(query);

        if (imGroupEntity == null) {
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_NOT_EXIST);
        }
        return ResponseVO.successResponse(imGroupEntity);
    }

    @Override
    public ResponseVO getGroup(GetGroupReq req) {

        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();

        query.eq("app_id", req.getAppId());
        query.in("group_id", req.getGroupId());
        List<ImGroupEntity> imGroupEntities = imGroupDataMapper.selectList(query);

        List<GetGroupResp> resp = new ArrayList<>(req.getGroupId().size());

        imGroupEntities.forEach(g -> {

            GetGroupResp getGroupResp = new GetGroupResp();
            BeanUtils.copyProperties(g, getGroupResp);
            try {
                ResponseVO<List<GroupMemberDto>> groupMember = groupMemberService.getGroupMember(g.getGroupId(), req.getAppId());
                if (groupMember.isOk()) {
                    getGroupResp.setMemberList(groupMember.getData());
                }
                resp.add(getGroupResp);
            } catch (Exception e) {

            }
        });
        return ResponseVO.successResponse(resp);
    }



    @Override
    public ResponseVO forbidSendMessageReq(ForbidSendMessageReq req) {

        ResponseVO<ImGroupEntity> groupResp = getGroup(req.getGroupId(), req.getAppId());
        if(!groupResp.isOk()){
            return groupResp;
        }

        boolean isadmin = false;

        if(!isadmin){
            //不是后台调用需要检查权限
            ResponseVO<GetRoleInGroupResp> role = groupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOperater(), req.getAppId());

            if (!role.isOk()) {
                return role;
            }

            GetRoleInGroupResp data = role.getData();
            Integer roleInfo = data.getRole();

            boolean isManager = roleInfo == GroupMemberRoleEnum.MAMAGER.getCode() || roleInfo == GroupMemberRoleEnum.OWNER.getCode();

            //公开群只能群主修改资料
            if (!isManager) {
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }
        }

        ImGroupEntity group = groupResp.getData();

        long seq = this.seq.getSeq(req.getAppId() + ":" + Constants.SeqConstants.Group);
        ImGroupEntity update = new ImGroupEntity();
        update.setMute(GroupMuteTypeEnum.MUTE.getCode());
        update.setGroupId(group.getGroupId());
        update.setSequence(seq);
        imGroupDataMapper.updateById(update);


        return ResponseVO.successResponse();
    }


}
