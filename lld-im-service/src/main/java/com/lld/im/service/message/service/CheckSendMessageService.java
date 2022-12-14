package com.lld.im.service.message.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.enums.*;
import com.lld.im.service.friendship.dao.ImFriendShipEntity;
import com.lld.im.service.friendship.model.req.GetRelationReq;
import com.lld.im.service.friendship.service.ImFriendShipService;
import com.lld.im.service.group.dao.ImGroupEntity;
import com.lld.im.service.group.model.resp.GetRoleInGroupResp;
import com.lld.im.service.group.service.ImGroupMemberService;
import com.lld.im.service.group.service.ImGroupService;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.service.ImUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-22 16:05
 **/
@Service
public class CheckSendMessageService {

    @Autowired
    ImUserService imUserService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    ImFriendShipService imFriendShipService;

    @Autowired
    ImGroupService groupService;

    @Autowired
    ImGroupMemberService groupMemberService;


    public ResponseVO checkSenderForbidAndMute(String from,Integer appId){
        ResponseVO<ImUserDataEntity> fromId = imUserService.getSingleUserInfo(from, appId);

        if(!fromId.isOk()){
            if(fromId.getCode() == UserErrorCode.USER_IS_NOT_EXIST.getCode()){
                return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_NOT_EXIST);
            }
        }

        ImUserDataEntity fromer = fromId.getData();
        // 禁用标识(0 未禁用 1 已禁用) silentFlag 1禁言
        if(fromer.getForbiddenFlag() == UserForbiddenFlagEnum.FORBIBBEN.getCode()){
            return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_FORBIBBEN);
        }else if(fromer.getSilentFlag() == UserSilentFlagEnum.MUTE.getCode()){
            return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_MUTE);
        }
        return ResponseVO.successResponse();
    }


    /**
     * @description 校验用户是否被禁用/发送方是否被禁言
     * @author chackylee
     * @date 2022/7/22 16:23
     * @param [from, to, appId]
     * @return com.lld.im.common.ResponseVO
    */
    public ResponseVO checkUserForbidAndMute(String from,String to,Integer appId){

        ResponseVO responseVO = checkSenderForbidAndMute(from, appId);
        if(!responseVO.isOk()){
            return responseVO;
        }

        ResponseVO<ImUserDataEntity> toId = imUserService.getSingleUserInfo(to, appId);
        if(!toId.isOk()){
            if(toId.getCode() == UserErrorCode.USER_IS_NOT_EXIST.getCode()){
                return ResponseVO.errorResponse(MessageErrorCode.TO_IS_NOT_EXIST);
            }
        }

//        ImUserDataEntity toer = toId.getData();
        // 禁用标识(0 未禁用 1 已禁用) silentFlag 1禁言
//        if(toer.getForbiddenFlag() == UserForbiddenFlagEnum.FORBIBBEN.getCode()){
//            return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_FORBIBBEN);
//        }else if(toer.getSilentFlag() == UserSilentFlagEnum.MUTE.getCode()){
//            return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_MUTE);
//        }

        return ResponseVO.successResponse();
    }

    /**
     * @description 校验双方关系链，
     * @author chackylee
     * @date 2022/7/27 8:44
     * @param [from, to, appId]
     * @return com.lld.im.common.ResponseVO
    */
    public ResponseVO checkFriendShip(String from,String to,Integer appId){

        if(appConfig.isSendMessageCheckFriend()){
            GetRelationReq fromReq = new GetRelationReq();
            fromReq.setFromId(from);
            fromReq.setToId(to);
            fromReq.setAppId(appId);
            ResponseVO<ImFriendShipEntity> fromResp = imFriendShipService.getRelation(fromReq);
            if(!fromResp.isOk()){
                return fromResp;
            }

            GetRelationReq toReq = new GetRelationReq();
            toReq.setFromId(to);
            toReq.setToId(from);
            toReq.setAppId(appId);
            ResponseVO<ImFriendShipEntity> toResp = imFriendShipService.getRelation(toReq);
            if(!toResp.isOk()){
                return toResp;
            }

            if(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getStatus() != fromResp.getData().getStatus()){
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }

            ImFriendShipEntity toData = toResp.getData();
            if(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getStatus() != toData.getStatus()){
                return ResponseVO.errorResponse(FriendShipErrorCode.TARGET_IS_DELETE_YOU);
            }

            if(appConfig.isSendMessageCheckFriend()){
                if(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getStatus() != fromResp.getData().getBlack()){
                    return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
                }
                if(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getStatus() != toData.getBlack()){
                    return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
                }
            }
        }

        return ResponseVO.successResponse();
    }

    /**
     * @description 个人是否被禁言 , 是否在群内, 群是否被禁言, 如果群被禁言是否是管理员or群主
     * @author chackylee
     * @date 2022/8/17 14:35
     * @param [from, to, appId]
     * @return com.lld.im.common.ResponseVO
    */
    public ResponseVO checkGroup(String from,String groupId,Integer appId){

        //校验发送方
        ResponseVO responseVO = checkSenderForbidAndMute(from, appId);
        if(!responseVO.isOk()){
            return responseVO;
        }

        ResponseVO<ImGroupEntity> group = groupService.getGroup(groupId, appId);
        if(!group.isOk()){
            return group;
        }

        //校验是否在群内
        ResponseVO<GetRoleInGroupResp> roleInGroupOne = groupMemberService.getRoleInGroupOne(groupId, from, appId);
        if(!roleInGroupOne.isOk()){
            return roleInGroupOne;
        }

        GetRoleInGroupResp data = roleInGroupOne.getData();
        if(data.getRole() == GroupMemberRoleEnum.LEAVE.getCode()){
            return ResponseVO.errorResponse(GroupErrorCode.MEMBER_IS_NOT_JOINED_GROUP);
        }

        ImGroupEntity groupData = group.getData();
        //校验群是否被禁言
        if (groupData.getMute() == GroupMuteTypeEnum.MUTE.getCode()
                && (data.getRole() == GroupMemberRoleEnum.MAMAGER.getCode()
                || data.getRole() == GroupMemberRoleEnum.OWNER.getCode())){
            return ResponseVO.errorResponse(GroupErrorCode.THIS_GROUP_IS_MUTE);
        }

        if(data.getSpeakDate() != null && data.getSpeakDate() > System.currentTimeMillis()){
            return ResponseVO.errorResponse(GroupErrorCode.GROUP_MEMBER_IS_SPEAK);
        }

        return ResponseVO.successResponse();
    }

}
