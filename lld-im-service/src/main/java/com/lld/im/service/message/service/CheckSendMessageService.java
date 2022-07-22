package com.lld.im.service.message.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.enums.MessageErrorCode;
import com.lld.im.common.enums.UserErrorCode;
import com.lld.im.common.enums.UserForbiddenFlagEnum;
import com.lld.im.common.enums.UserSilentFlagEnum;
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

        ImUserDataEntity toer = toId.getData();
        // 禁用标识(0 未禁用 1 已禁用) silentFlag 1禁言
//        if(toer.getForbiddenFlag() == UserForbiddenFlagEnum.FORBIBBEN.getCode()){
//            return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_FORBIBBEN);
//        }else if(toer.getSilentFlag() == UserSilentFlagEnum.MUTE.getCode()){
//            return ResponseVO.errorResponse(MessageErrorCode.FROMER_IS_MUTE);
//        }

        return ResponseVO.successResponse();
    }

}
