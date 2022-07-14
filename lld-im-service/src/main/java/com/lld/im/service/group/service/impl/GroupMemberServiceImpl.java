package com.lld.im.service.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.enums.GroupErrorCode;
import com.lld.im.common.enums.GroupMemberRoleEnum;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.service.Application;
import com.lld.im.service.group.dao.ImGroupMemberEntity;
import com.lld.im.service.group.dao.mapper.ImGroupMemberMapper;
import com.lld.im.service.group.model.req.GetRoleInGroupReq;
import com.lld.im.service.group.model.req.GroupMemberDto;
import com.lld.im.service.group.model.resp.GetRoleInGroupResp;
import com.lld.im.service.group.service.GroupMemberService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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


    /**
     * @description:
     * @param
     * @return com.lld.im.common.ResponseVO
     * @author lld
     * @since 2022/7/10
     */
    @Override
    @Transactional
    public ResponseVO addGroupMember(String groupId, Integer appId, GroupMemberDto dto ) {

        QueryWrapper<ImGroupMemberEntity> query = new QueryWrapper<>();
        query.eq("group_id",groupId);
        query.eq("app_id",appId);
        query.eq("member_id",dto.getMemberId());

        ImGroupMemberEntity memberDto = imGroupMemberMapper.selectOne(query);

        if(dto.getRole() == GroupMemberRoleEnum.OWNER.getCode()){
            QueryWrapper<ImGroupMemberEntity> queryOwner = new QueryWrapper<>();
            queryOwner.eq("group_id",groupId);
            queryOwner.eq("app_id",appId);
            queryOwner.eq("role",GroupMemberRoleEnum.OWNER.getCode());
            Integer ownerNum = imGroupMemberMapper.selectCount(queryOwner);
            if(ownerNum > 0){
                return ResponseVO.errorResponse(GroupErrorCode.GROUP_IS_HAVE_OWNER);
            }
        }

        long now = System.currentTimeMillis();
        if(memberDto == null){
            //初次加群
            memberDto = new ImGroupMemberEntity();
            BeanUtils.copyProperties(dto,memberDto);
//            memberDto.setMemberId(dto.getMemberId());
            memberDto.setGroupId(groupId);
            memberDto.setAppId(appId);
            memberDto.setJoinTime(now);
            int insert = imGroupMemberMapper.insert(memberDto);
            if(insert == 1){
                return ResponseVO.successResponse();
            }
            return ResponseVO.errorResponse(GroupErrorCode.USER_JOIN_GROUP_ERROR);
        }else{
            //重新进群
            memberDto = new ImGroupMemberEntity();
            BeanUtils.copyProperties(dto,memberDto);
            memberDto.setJoinTime(now);
            int update = imGroupMemberMapper.update(memberDto, query);
            if(update == 1){
                return ResponseVO.successResponse();
            }
            return ResponseVO.errorResponse(GroupErrorCode.USER_JOIN_GROUP_ERROR);
        }

    }

    @Override
    public ResponseVO removeGroupMember(String groupId, Integer appId, GroupMemberDto dto) {
        return null;
    }

    @Override
    public ResponseVO<GetRoleInGroupResp> getRoleInGroupOne(String groupId, String memberId, Integer appId) {

        QueryWrapper<ImGroupMemberEntity> queryOwner = new QueryWrapper<>();
        queryOwner.eq("group_id",groupId);
        queryOwner.eq("app_id",appId);
        queryOwner.eq("member_id",memberId);

        ImGroupMemberEntity imGroupMemberEntity = imGroupMemberMapper.selectOne(queryOwner);
        if(imGroupMemberEntity == null || imGroupMemberEntity.getRole() == GroupMemberRoleEnum.LEAVE.getCode()){
            return ResponseVO.errorResponse(GroupErrorCode.MEMBER_IS_NOT_JOINED_GROUP);
        }
        return ResponseVO.successResponse(imGroupMemberEntity);
    }

    @Override
    public ResponseVO<List<GetRoleInGroupResp>> getRoleInGroup(GetRoleInGroupReq req) {
        return null;
    }


}
