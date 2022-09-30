package com.lld.im.service.group.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.GroupErrorCode;
import com.lld.im.common.enums.GroupMemberRoleEnum;
import com.lld.im.common.enums.GroupTypeEnum;
import com.lld.im.common.exception.ApplicationException;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
//            memberDto.setMemberId(dto.getMemberId());
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
     * @description: 添加群成员，拉人入群的逻辑，如果是后台管理员，则直接拉入群，如果不是则根据群类型是私有群/公开群走不同的逻辑。
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
         * 公开群（Public）	类似 QQ 群，创建后群主可以指定群管理员，用户搜索群 ID 发起加群申请后，需要群主或管理员审批通过才能入群
         * 群类型 1私有群（类似微信） 2公开群(类似qq）
         * 加入群权限，0 所有人可以加入；1 群成员可以拉人；2 群管理员或群组可以拉人。私有群任何人都可以拉人入群。公开群需要管理员或群主审批
         * 当为公开群时 applyJoinType 生效。
         *
         */
        if(!isAdmin){
             if(GroupTypeEnum.PUBLIC.getCode() == group.getGroupType()){
                 //不是群成员无法拉人入群
                 ResponseVO<GetRoleInGroupResp> role = getRoleInGroupOne(req.getGroupId(), req.getOperater(), req.getAppId());
                 if(!role.isOk()){
                     return role;
                 }

                 GetRoleInGroupResp data = role.getData();
                 Integer roleInfo = data.getRole();

                 boolean isManager = roleInfo == GroupMemberRoleEnum.MAMAGER.getCode() || roleInfo == GroupMemberRoleEnum.OWNER.getCode();
                //公开群必须是管理员才能拉人
                 if(!isManager && GroupTypeEnum.PRIVATE.getCode() == group.getGroupType()){
                     throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
                 }
            }
        }

        List<String> successId = new ArrayList<>();
        for (GroupMemberDto memberId:
             req.getMembers()) {

            ResponseVO responseVO = groupMemberService.addGroupMember(req.getGroupId(), req.getAppId(), memberId);
            AddMemberResp addMemberResp = new AddMemberResp();
            addMemberResp.setMemberId(memberId.getMemberId());
            if(responseVO.isOk()){
                addMemberResp.setResult(0);
            }else if(responseVO.getCode() == GroupErrorCode.USER_IS_JOINED_GROUP.getCode()){
                addMemberResp.setResult(2);
            }else{
                successId.add(memberId.getMemberId());
                addMemberResp.setResult(1);
            }
            resp.add(addMemberResp);
        }

        AddMemberCallback addMemberCallback = new AddMemberCallback();
        addMemberCallback.setGroupId(req.getGroupId());
        addMemberCallback.setGroupType(group.getGroupType());
        addMemberCallback.setMemberId(successId);
        addMemberCallback.setOperater(req.getOperater());
        addMemberCallback.setJoinType(1);
        callbackService.callback(req.getAppId(), Constants.CallbackCommand.GroupMemberAdd, JSONObject.toJSONString(addMemberCallback));
        //TODO tcp通知
//        groupMessageProducer.producer();
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
                //不是群成员无法拉人入群
                ResponseVO<GetRoleInGroupResp> role = getRoleInGroupOne(req.getGroupId(), req.getOperater(), req.getAppId());
                if(!role.isOk()){
                    return role;
                }

                GetRoleInGroupResp data = role.getData();
                Integer roleInfo = data.getRole();

                boolean isManager = roleInfo == GroupMemberRoleEnum.MAMAGER.getCode()
                        || roleInfo == GroupMemberRoleEnum.OWNER.getCode();
                //公开群必须是管理员才能踢人
                if(!isManager && GroupTypeEnum.PRIVATE.getCode() == group.getGroupType()){
                    throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
                }

                ResponseVO<GetRoleInGroupResp> roleInGroupOne = this.getRoleInGroupOne(req.getGroupId(), req.getMemberId(), req.getAppId());
                if(!roleInGroupOne.isOk()){
                    return roleInGroupOne;
                }

                GetRoleInGroupResp memberRole = roleInGroupOne.getData();
                if(memberRole.getRole() == GroupMemberRoleEnum.OWNER.getCode()){
                    throw new ApplicationException(GroupErrorCode.GROUP_OWNER_IS_NOT_REMOVE);
                }
            }
        }


        ResponseVO responseVO = groupMemberService.removeGroupMember(req.getGroupId(), req.getAppId(), req.getMemberId());

        return responseVO;
    }

    @Override
    public ResponseVO<List<GroupMemberDto>> getGroupMember(String groupId,Integer appId) {
        List<GroupMemberDto> groupMember = imGroupMemberMapper.getGroupMember(appId, groupId);
        return ResponseVO.successResponse(groupMember);
    }

    @Override
    public List<GroupMemberDto> getGroupManager(String groupId, Integer appId) {
        return imGroupMemberMapper.getGroupManager(groupId,appId);
    }


}
