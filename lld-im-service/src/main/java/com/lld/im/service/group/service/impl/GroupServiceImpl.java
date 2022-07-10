package com.lld.im.service.group.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.enums.GroupErrorCode;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.service.config.AppConfig;
import com.lld.im.service.group.dao.ImGroupEntity;
import com.lld.im.service.group.dao.mapper.ImGroupMapper;
import com.lld.im.service.group.model.req.CreateGroupReq;
import com.lld.im.service.group.model.req.GroupMemberDto;
import com.lld.im.service.group.service.GroupMemberService;
import com.lld.im.service.group.service.GroupService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/9
 * @version: 1.0
 */
@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    ImGroupMapper imGroupDataMapper;

    @Autowired
    GroupMemberService groupMemberService;

    @Autowired
    AppConfig appConfig;

    @Override
    @Transactional
    public ResponseVO createGroup(CreateGroupReq req) {

        //1.判断群id是否存在
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();

        if(StringUtils.isEmpty(req.getGroupId())){
            req.setGroupId(UUID.randomUUID().toString().replace("-",""));
        } else{
            query.eq("group_id",req.getGroupId());
            query.eq("app_id",req.getAppId());
            Integer integer = imGroupDataMapper.selectCount(query);
            if(integer > 0){
                throw new ApplicationException(GroupErrorCode.GROUP_IS_EXIST);
            }
        }

        if(req.getMaxMemberCount() != null && req.getMaxMemberCount() > appConfig.getGroupMaxMemberCount()){
            throw new ApplicationException(GroupErrorCode.GROUP_MEMBER_IS_BEYOND);
        }

        if(req.getMember().size() > appConfig.getGroupMaxMemberCount()){
            throw new ApplicationException(GroupErrorCode.GROUP_MEMBER_IS_BEYOND);
        }

        ImGroupEntity imGroupEntity = new ImGroupEntity();
        BeanUtils.copyProperties(req,imGroupEntity);
        int insert = imGroupDataMapper.insert(imGroupEntity);
        //插入群成员

        for (GroupMemberDto dto : req.getMember()) {
            groupMemberService.addGroupMember(req.getGroupId(),req.getAppId(),dto);
        }

        return ResponseVO.successResponse();
    }
}
