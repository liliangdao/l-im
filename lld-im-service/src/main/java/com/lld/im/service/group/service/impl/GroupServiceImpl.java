package com.lld.im.service.group.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lld.im.common.BaseErrorCode;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.GroupErrorCode;
import com.lld.im.common.enums.GroupMemberRoleEnum;
import com.lld.im.common.enums.GroupMuteTypeEnum;
import com.lld.im.common.enums.GroupPrivateChatTypeEnum;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.service.group.dao.ImGroupEntity;
import com.lld.im.service.group.dao.mapper.ImGroupMapper;
import com.lld.im.service.group.model.req.CreateGroupReq;
import com.lld.im.service.group.model.req.GroupMemberDto;
import com.lld.im.service.group.model.req.UpdateGroupReq;
import com.lld.im.service.group.model.resp.GetRoleInGroupResp;
import com.lld.im.service.group.service.GroupMemberService;
import com.lld.im.service.group.service.GroupService;
import com.lld.im.service.utils.CallbackService;
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

    @Autowired
    CallbackService callbackService;

    @Override
    @Transactional
    public ResponseVO createGroup(CreateGroupReq req) {

        boolean isAdmin = false;

        if(!isAdmin){
            req.setOwnerId(req.getOperater());
        }

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

        //TODO 发送tcp通知

        //回调
        callbackService.callback(req.getAppId(), Constants.CallbackCommand.CreateGroup, JSONObject.toJSONString(imGroupEntity));
        return ResponseVO.successResponse();
    }

    /**
     * @description 修改群基础信息，如果是后台管理员调用，则不检查权限，如果不是则检查权限，如果是私有群（微信群）用户可以修改名称其他不能修改
     *               如果是群主或者管理员可以修改其他信息。
     * @author chackylee
     * @date 2022/7/14 10:11
     * @param [req]
     * @return com.lld.im.common.ResponseVO
    */
    @Override
    @Transactional
    public ResponseVO updateBaseGroupInfo(UpdateGroupReq req) {

        //1.判断群id是否存在
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        query.eq("group_id",req.getGroupId());
        query.eq("app_id",req.getAppId());
        ImGroupEntity imGroupEntity = imGroupDataMapper.selectOne(query);
        if (imGroupEntity == null) {
            throw new ApplicationException(GroupErrorCode.GROUP_IS_EXIST);
        }

        boolean isAdmin = false;

        if(!isAdmin){
            //不是后台调用需要检查权限
            ResponseVO<GetRoleInGroupResp> role = groupMemberService.getRoleInGroupOne(req.getGroupId(), req.getOperater(), req.getAppId());
            GetRoleInGroupResp data = role.getData();
            Integer roleInfo = data.getRole();

            boolean isManager = roleInfo == GroupMemberRoleEnum.MAMAGER.getCode() || roleInfo == GroupMemberRoleEnum.OWNER.getCode();
            if((StringUtils.isNotBlank(req.getIntroduction()) ||
                    StringUtils.isNotBlank(req.getNotification()) ||
                    GroupPrivateChatTypeEnum.getEnum(req.getPrivateChat()) != null ||
                    StringUtils.isNotBlank(req.getPhoto()) || GroupPrivateChatTypeEnum.getEnum(req.getPrivateChat()) != null ||
                    GroupMuteTypeEnum.getEnum(req.getJoinType()) != null) && !isManager){
                throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
            }
        }

        ImGroupEntity update = new ImGroupEntity();
        BeanUtils.copyProperties(req,update);
        int row = imGroupDataMapper.update(update, query);
        if(row != 1){
            throw new ApplicationException(GroupErrorCode.THIS_OPERATE_NEED_MANAGER_ROLE);
        }

        //TODO 发送Tcp通知
        callbackService.callback(req.getAppId(), Constants.CallbackCommand.UpdateGroup, JSONObject.toJSONString(imGroupDataMapper.selectOne(query)));
        return ResponseVO.successResponse();
    }


}
