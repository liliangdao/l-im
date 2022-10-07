package com.lld.im.service.group.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lld.im.codec.pack.AddGroupMemberPack;
import com.lld.im.codec.pack.RemoveGroupMemberPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.GroupErrorCode;
import com.lld.im.common.enums.GroupMemberRoleEnum;
import com.lld.im.common.enums.GroupTypeEnum;
import com.lld.im.common.enums.command.GroupEventCommand;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.SyncReq;
import com.lld.im.service.group.dao.ImGroupEntity;
import com.lld.im.service.group.dao.ImGroupMemberEntity;
import com.lld.im.service.group.dao.mapper.ImGroupMemberMapper;
import com.lld.im.service.group.model.callback.AddMemberCallback;
import com.lld.im.service.group.model.req.*;
import com.lld.im.service.group.model.resp.AddMemberResp;
import com.lld.im.service.group.model.resp.GetRoleInGroupResp;
import com.lld.im.service.group.service.GroupMemberService;
import com.lld.im.service.group.service.GroupMessageProducer;
import com.lld.im.service.group.service.GroupService;
import com.lld.im.service.message.service.MessageProducer;
import com.lld.im.service.utils.CallbackService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.security.auth.login.Configuration;
import java.util.*;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/10
 * @version: 1.0
 */
@Service
public class GroupMemberServiceImpl implements GroupMemberService {

    @Autowired
    ImGroupMemberMapper imGroupMemberMapper;

    @Autowired
    GroupService groupService;

    @Autowired
    GroupMemberService groupMemberService;

    @Autowired
    CallbackService callbackService;

    @Autowired
    GroupMessageProducer groupMessageProducer;

    @Autowired
    AppConfig appConfig;

    @Override
    public ResponseVO importGroupMember(ImportGroupMemberReq req) {

        List<AddMemberResp> resp = new ArrayList<>();

        ResponseVO<ImGroupEntity> groupResp = groupService.getGroup(req.getGroupId(), req.getAppId());
        if(!groupResp.isOk()){
            return groupResp;
        }

        List<String> successId = new ArrayList<>();
        for (GroupMemberDto memberId:
                req.getMembers()) {
            ResponseVO responseVO = null;
            try {
                responseVO = groupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), memberId);
            }catch (Exception e){
                e.printStackTrace();
                responseVO = ResponseVO.errorResponse();
            }
            AddMemberResp addMemberResp = new AddMemberResp();
            addMemberResp.setMemberId(memberId.getMemberId());
            if(responseVO.isOk()){
                successId.add(memberId.getMemberId());
                addMemberResp.setResult(0);
            }else if(responseVO.getCode() == GroupErrorCode.USER_IS_JOINED_GROUP.getCode()){
                addMemberResp.setResult(2);
            }else{
                addMemberResp.setResult(1);
            }
            resp.add(addMemberResp);
        }

        return ResponseVO.successResponse(resp);
    }

    /**
     * @param
     * @return com.lld.im.common.ResponseVO
     * @description: 添加群成员，内部调用
     * @author lld
     * @since 2022/7/10
     */
    @Override
    @Transactional
    public ResponseVO addGroupMember(String groupId, Integer appId, GroupMemberDto dto) {

        QueryWrapper<ImGroupMemberEntity> query = new QueryWrapper<>();
        query.eq("group_id", groupId);
        query.eq("app_id", appId);
        query.eq("member_id", dto.getMemberId());

        if (dto.getRole() != null && GroupMemberRoleEnum.OWNER.getCode() == dto.getRole()) {
            QueryWrapper<ImGroupMemberEntity> queryOwner = new QueryWrapper<>();
            queryOwner.eq("group_id", groupId);
            queryOwner.eq("app_id", appId);
            queryOwner.eq("role", GroupMemberRoleEnum.OWNER.getCode());
            Integer ownerNum = imGroupMemberMapper.selectCount(queryOwner);
            if (ownerNum > 0) {
                return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_HAVE_OWNER);
            }
        }

        ImGroupMemberEntity memberDto = imGroupMemberMapper.selectOne(query);

        long now = System.currentTimeMillis();
        if (memberDto == null) {
            //初次加群
            memberDto = new ImGroupMemberEntity();
            BeanUtils.copyProperties(dto, memberDto);
            memberDto.setGroupId(groupId);
            memberDto.setAppId(appId);
            memberDto.setJoinTime(now);
            int insert = imGroupMemberMapper.insert(memberDto);
            if (insert == 1) {
                return ResponseVO.successResponse();
            }
            return ResponseVO.errorResponse(GroupErrorCode.USER_JOIN_GROUP_ERROR);
        } else if(GroupMemberRoleEnum.LEAVE.getCode() == memberDto.getRole()){
            //重新进群
            memberDto = new ImGroupMemberEntity();
            BeanUtils.copyProperties(dto, memberDto);
            memberDto.setJoinTime(now);
            int update = imGroupMemberMapper.update(memberDto, query);
            if (update == 1) {
                return ResponseVO.successResponse();
            }
            return ResponseVO.errorResponse(GroupErrorCode.USER_JOIN_GROUP_ERROR);
        }

        return ResponseVO.errorResponse(GroupErrorCode.USER_IS_JOINED_GROUP);

    }

    @Override
    public ResponseVO removeGroupMember(String groupId, Integer appId, String memberId) {
        return null;
    }

    /**
     * @description 查询用户在群内的角色
     * @author chackylee
     * @date 2022/8/17 14:40
     * @param [groupId, memberId, appId]
     * @return com.lld.im.common.ResponseVO<com.lld.im.service.group.model.resp.GetRoleInGroupResp>
    */
    @Override
    public ResponseVO<GetRoleInGroupResp> getRoleInGroupOne(String groupId, String memberId, Integer appId) {

        GetRoleInGroupResp resp = new GetRoleInGroupResp();

        QueryWrapper<ImGroupMemberEntity> queryOwner = new QueryWrapper<>();
        queryOwner.eq("group_id", groupId);
        queryOwner.eq("app_id", appId);
        queryOwner.eq("member_id", memberId);

        ImGroupMemberEntity imGroupMemberEntity = imGroupMemberMapper.selectOne(queryOwner);
        if (imGroupMemberEntity == null || imGroupMemberEntity.getRole() == GroupMemberRoleEnum.LEAVE.getCode()) {
            return ResponseVO.errorResponse(GroupErrorCode.MEMBER_IS_NOT_JOINED_GROUP);
        }

        resp.setMemberId(imGroupMemberEntity.getMemberId());
        resp.setRole(imGroupMemberEntity.getRole());
        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO<List<GetRoleInGroupResp>> getRoleInGroup(GetRoleInGroupReq req) {
        return null;
    }

    @Override
    public ResponseVO<Collection<String>> getMemberJoinedGroup(GetJoinedGroupReq req) {

        if (req.getLimit() != null) {
            Page<ImGroupMemberEntity> objectPage = new Page<>(req.getOffset(), req.getLimit());
            QueryWrapper<ImGroupMemberEntity> query = new QueryWrapper<>();
            query.eq("app_id", req.getAppId());
            query.eq("member_id", req.getMemberId());
            IPage<ImGroupMemberEntity> imGroupMemberEntityPage = imGroupMemberMapper.selectPage(objectPage, query);

            Set<String> groupId = new HashSet<>();
            List<ImGroupMemberEntity> records = imGroupMemberEntityPage.getRecords();
            records.forEach(e -> {
                groupId.add(e.getGroupId());
            });

            return ResponseVO.successResponse(groupId);
        } else {
            return ResponseVO.successResponse(imGroupMemberMapper.getJoinedGroupId(req.getAppId(),req.getMemberId()));
        }
    }

    /**
     * @description
     * @author chackylee
     * @date 2022/8/18 10:13
     * @param [req]
     * @return com.lld.im.common.ResponseVO<java.util.Collection<java.lang.String>>
    */
    @Override
    public ResponseVO<Collection<String>> syncMemberJoinedGroup(SyncReq req) {
        return ResponseVO.successResponse(imGroupMemberMapper.syncJoinedGroupId(req.getAppId(),req.getOperater(),GroupMemberRoleEnum.LEAVE.getCode()));
    }

    /**
     * @description: 添加群成员，拉人入群的逻辑，直接进入群聊。如果是后台管理员，则直接拉入群，否则只有私有群可以调用本接口，并且群成员也可以拉人入群
     * @param
     * @return com.lld.im.common.ResponseVO
     * @author lld
     * @since 2022/7/16
     */
    @Override
    public ResponseVO addMember(AddGroupMemberReq req) {

        List<AddMemberResp> resp = new ArrayList<>();

        boolean isAdmin = false;
        ResponseVO<ImGroupEntity> groupResp = groupService.getGroup(req.getGroupId(), req.getAppId());
        if(!groupResp.isOk()){
            return groupResp;
        }

        ImGroupEntity group = groupResp.getData();

        /**
         * 私有群（private）	类似普通微信群，创建后仅支持已在群内的好友邀请加群，且无需被邀请方同意或群主审批
         * 公开群（Public）	类似 QQ 群，创建后群主可以指定群管理员，需要群主或管理员审批通过才能入群
         * 群类型 1私有群（类似微信） 2公开群(类似qq）
         * 只有私有群可以调用本接口
         */
//        if(!isAdmin){
//             if(GroupTypeEnum.PUBLIC.getCode() == group.getGroupType()){
//                 //不是群成员无法拉人入群
//                 ResponseVO<GetRoleInGroupResp> role = getRoleInGroupOne(req.getGroupId(), req.getOperater(), req.getAppId());
//                 if(!role.isOk()){
//                     return role;
//                 }
//
//                 GetRoleInGroupResp data = role.getData();
//                 Integer roleInfo = data.getRole();
//
//                 boolean isManager = roleInfo == GroupMemberRoleEnum.MAMAGER.getCode() || roleInfo == GroupMemberRoleEnum.OWNER.getCode();
//                //公开群必须是管理员才能拉人
//                 if(!isManager && GroupTypeEnum.PRIVATE.getCode() == group.getGroupType()){
//                     throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
//                 }
//            }
//        }

        if(!isAdmin && GroupTypeEnum.PUBLIC.getCode() == group.getGroupType()) {
            throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_APPMANAGER_ROLE);
        }

        List<String> successId = new ArrayList<>();
//        if(CollectionUtil.isNotEmpty(req.getMembers())){
            for (GroupMemberDto memberId:
                    req.getMembers()) {
                ResponseVO responseVO = null;
                try {
                    responseVO = groupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), memberId);
                }catch (Exception e){
                    e.printStackTrace();
                    responseVO = ResponseVO.errorResponse();
                }
                AddMemberResp addMemberResp = new AddMemberResp();
                addMemberResp.setMemberId(memberId.getMemberId());
                if(responseVO.isOk()){
                    successId.add(memberId.getMemberId());
                    addMemberResp.setResult(0);
                }else if(responseVO.getCode() == GroupErrorCode.USER_IS_JOINED_GROUP.getCode()){
                    addMemberResp.setResult(2);
                }else{
                    addMemberResp.setResult(1);
                }
                resp.add(addMemberResp);
            }
//        }

        AddGroupMemberPack addGroupMemberPack = new AddGroupMemberPack();
        addGroupMemberPack.setGroupId(req.getGroupId());
        addGroupMemberPack.setMembers(successId);
        groupMessageProducer.producer(req.getOperater(),GroupEventCommand.ADDED_MEMBER,addGroupMemberPack
                ,new ClientInfo(req.getAppId(),req.getClientType(),req.getImel()));

        if(appConfig.isAddGroupMemberCallback()){
            AddMemberCallback addMemberCallback = new AddMemberCallback();
            addMemberCallback.setGroupId(req.getGroupId());
            addMemberCallback.setGroupType(group.getGroupType());
            addMemberCallback.setMemberId(successId);
            addMemberCallback.setOperater(req.getOperater());
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.GroupMemberAdd, JSONObject.toJSONString(addMemberCallback));
        }

        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO removeMember(RemoveGroupMemberReq req) {

        List<AddMemberResp> resp = new ArrayList<>();
        boolean isAdmin = false;
        ResponseVO<ImGroupEntity> groupResp = groupService.getGroup(req.getGroupId(), req.getAppId());
        if(!groupResp.isOk()){
            return groupResp;
        }

        ImGroupEntity group = groupResp.getData();

        if(!isAdmin){
            if(GroupTypeEnum.PUBLIC.getCode() == group.getGroupType()){

                //获取操作人的权限 是管理员or群主or群成员
                ResponseVO<GetRoleInGroupResp> role = getRoleInGroupOne(req.getGroupId(), req.getOperater(), req.getAppId());
                if(!role.isOk()){
                    return role;
                }

                GetRoleInGroupResp data = role.getData();
                Integer roleInfo = data.getRole();

                boolean isOwner = roleInfo == GroupMemberRoleEnum.OWNER.getCode();
                boolean isManager = roleInfo == GroupMemberRoleEnum.MAMAGER.getCode();

                if(!isOwner && !isManager){
                    throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
                }

                //私有群必须是群主才能踢人
                if(!isOwner && GroupTypeEnum.PRIVATE.getCode() == group.getGroupType()){
                    throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
                }

                //公开群管理员和群主可踢人，但管理员只能踢普通群成员
                if(GroupTypeEnum.PUBLIC.getCode() == group.getGroupType()){
//                    throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
                    //获取被踢人的权限
                    ResponseVO<GetRoleInGroupResp> roleInGroupOne = this.getRoleInGroupOne(req.getGroupId(), req.getMemberId(), req.getAppId());
                    if(!roleInGroupOne.isOk()){
                        return roleInGroupOne;
                    }
                    GetRoleInGroupResp memberRole = roleInGroupOne.getData();
                    if(memberRole.getRole() == GroupMemberRoleEnum.OWNER.getCode()){
                        throw new ApplicationException(GroupErrorCode.GROUP_OWNER_IS_NOT_REMOVE);
                    }
                    //是管理员并且被踢人不是群成员，无法操作
                    if(isManager && memberRole.getRole() != GroupMemberRoleEnum.ORDINARY.getCode()){
                        throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_OWNER_ROLE);
                    }
                }
            }
        }


        ResponseVO responseVO = groupMemberService.removeGroupMember(req.getGroupId(), req.getAppId(), req.getMemberId());
        if(responseVO.isOk()){
            RemoveGroupMemberPack removeGroupMemberPack = new RemoveGroupMemberPack();
            removeGroupMemberPack.setGroupId(req.getGroupId());
            removeGroupMemberPack.setMember(req.getMemberId());
            groupMessageProducer.producer(req.getMemberId(),GroupEventCommand.DELETED_MEMBER,removeGroupMemberPack
            ,new ClientInfo(req.getAppId(),req.getClientType(),req.getImel()));
            if(appConfig.isDeleteGroupMemberCallback()){
                callbackService.callback(req.getAppId(), Constants.CallbackCommand.GroupMemberDelete,JSONObject.toJSONString(req));
            }
        }
        return responseVO;
    }

    @Override
    public ResponseVO<List<GroupMemberDto>> getGroupMember(String groupId,Integer appId) {
        List<GroupMemberDto> groupMember = imGroupMemberMapper.getGroupMember(appId, groupId);
        return ResponseVO.successResponse(groupMember);
    }

    @Override
    public List<String> getGroupMemberId(String groupId, Integer appId) {
        return imGroupMemberMapper.getGroupMemberId(appId,groupId);
    }

    @Override
    public List<GroupMemberDto> getGroupManager(String groupId, Integer appId) {
        return imGroupMemberMapper.getGroupManager(groupId,appId);
    }

    @Override
    public ResponseVO updateGroupMember(UpdateGroupMemberReq req) {

        boolean isadmin = false;

        ResponseVO<ImGroupEntity> group = groupService.getGroup(req.getGroupId(), req.getAppId());
        if(!group.isOk()){
            return group;
        }

        ImGroupEntity groupData = group.getData();

        //是否是自己修改自己的资料
        boolean isMeOperate = req.getOperater().equals(req.getMemberId());

        if(isadmin){
            //昵称只能自己修改 权限只能群主或管理员修改
            if(StringUtils.isBlank(req.getAlias()) && isMeOperate){
                return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_ONESELF);
            }
            //私有群不能设置管理员
            if(groupData.getGroupType() == GroupTypeEnum.PRIVATE.getCode() &&
                    req.getRole() != null && GroupMemberRoleEnum.getItem(req.getRole()) != null){
                return ResponseVO.errorResponse(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }
        }

        ImGroupMemberEntity update = new ImGroupMemberEntity();
        if(req.getRole() != null && GroupMemberRoleEnum.getItem(req.getRole()) != null ){
            update.setRole(req.getRole());
        }

        if(StringUtils.isNotBlank(req.getAlias())){
            update.setAlias(req.getAlias());
        }

        UpdateWrapper<ImGroupMemberEntity> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.eq("app_id",req.getAppId());
        objectUpdateWrapper.eq("member_id",req.getMemberId());
        objectUpdateWrapper.eq("group_id",req.getGroupId());
        imGroupMemberMapper.update(update, objectUpdateWrapper);
        return ResponseVO.successResponse();
    }

    @Override
    @Transactional
    public ResponseVO transferGroupMember(String owner, String groupId, Integer appId) {

        //更新旧群主
        ImGroupMemberEntity imGroupMemberEntity = new ImGroupMemberEntity();
        imGroupMemberEntity.setRole(GroupMemberRoleEnum.ORDINARY.getCode());
        UpdateWrapper<ImGroupMemberEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("app_id",appId);
        updateWrapper.eq("group_id",groupId);
        updateWrapper.eq("role",GroupMemberRoleEnum.OWNER.getCode());
        imGroupMemberMapper.update(imGroupMemberEntity,updateWrapper);

        //更新新群主
        ImGroupMemberEntity newOwner = new ImGroupMemberEntity();
        newOwner.setRole(GroupMemberRoleEnum.OWNER.getCode());
        UpdateWrapper<ImGroupMemberEntity> ownerWrapper = new UpdateWrapper<>();
        ownerWrapper.eq("app_id",appId);
        ownerWrapper.eq("group_id",groupId);
        ownerWrapper.eq("member_id",owner);
        imGroupMemberMapper.update(newOwner,ownerWrapper);

        return ResponseVO.successResponse();
    }



}
