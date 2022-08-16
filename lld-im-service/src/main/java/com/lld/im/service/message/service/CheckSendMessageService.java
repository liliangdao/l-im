package com.lld.im.service.message.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.enums.*;
import com.lld.im.service.friendship.dao.ImFriendShipEntity;
import com.lld.im.service.friendship.model.req.GetRelationReq;
import com.lld.im.service.friendship.service.ImFriendShipService;
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

    /**
     * @description 校验用户是否被禁用/发送方是否被禁言
     * @author chackylee
     * @date 2022/7/22 16:23
     * @param [from, to, appId]
     * @return com.lld.im.common.ResponseVO
    */
    public ResponseVO checkUserForbidAndMute(String from,String to,Integer appId){

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

            if(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() != fromResp.getData().getStatus()){
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
            }

            ImFriendShipEntity toData = toResp.getData();
            if(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode() != toData.getStatus()){
                return ResponseVO.errorResponse(FriendShipErrorCode.TARGET_IS_DELETE_YOU);
            }

            if(appConfig.isSendMessageCheckFriend()){
                if(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode() != fromResp.getData().getBlack()){
                    return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
                }
                if(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode() != toData.getBlack()){
                    return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_IS_DELETED);
                }
            }
        }

        return ResponseVO.successResponse();
    }

}
