package com.lld.im.service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lld.im.common.ResponseVO;
import com.lld.im.service.dao.ImFriendShipRequestEntity;
import com.lld.im.service.dao.mapper.ImFriendShipRequestMapper;
import com.lld.im.common.enums.FriendShipErrorCode;
import com.lld.im.service.model.req.friendship.FriendDto;
import com.lld.im.service.service.ImFriendShipRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lld.im.service.dao.ImFriendShipEntity;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 14:25
 **/
@Service
public class ImFriendShipRequestServiceImpl implements ImFriendShipRequestService {


    @Autowired
    ImFriendShipRequestMapper imFriendShipRequestMapper;


    @Override
    @Transactional
    public ResponseVO addFriendRequest(String fromId, Integer appId, FriendDto dto) {

        QueryWrapper query = new QueryWrapper();
        query.eq("from_id",fromId);
        query.eq("to_id",dto.getToId());
        query.eq("app_id",appId);
        ImFriendShipRequestEntity request = imFriendShipRequestMapper.selectOne(query);

        if(request == null){
            request = new ImFriendShipRequestEntity();
            request.setAddWording(dto.getAddWording());
            request.setAddSource(dto.getAddSource());
            request.setAppId(appId);
            request.setCreateTime(System.currentTimeMillis());
            request.setFromId(fromId);
            request.setReadStatus(0);
            request.setRemark(dto.getRemark());
            request.setToId(dto.getToId());
            int insert = imFriendShipRequestMapper.insert(request);
            if(insert < 0){
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_REQUEST_ERROR.getCode(),
                        FriendShipErrorCode.ADD_FRIEND_REQUEST_ERROR.getError());
            }
        }else{
            request.setAddWording(dto.getAddWording());
            int i = imFriendShipRequestMapper.updateById(request);
//            if(i < 0){
//                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_REQUEST_ERROR.getCode(),
//                        FriendShipErrorCode.ADD_FRIEND_REQUEST_ERROR.getError());
//            }
        }

        return ResponseVO.successResponse();
    }
}
