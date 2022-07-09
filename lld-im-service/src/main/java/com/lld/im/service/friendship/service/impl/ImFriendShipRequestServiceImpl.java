package com.lld.im.service.friendship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.Query;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.enums.UserErrorCode;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.service.friendship.dao.ImFriendShipRequestEntity;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipRequestMapper;
import com.lld.im.common.enums.FriendShipErrorCode;
import com.lld.im.service.friendship.model.req.FriendDto;
import com.lld.im.service.friendship.model.resp.GetFriendRequestResp;
import com.lld.im.service.friendship.service.ImFriendShipRequestService;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.model.req.GetUserInfoReq;
import com.lld.im.service.user.model.resp.GetUserInfoResp;
import com.lld.im.service.user.service.ImUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 14:25
 **/
@Service
public class ImFriendShipRequestServiceImpl implements ImFriendShipRequestService {


    @Autowired
    ImFriendShipRequestMapper imFriendShipRequestMapper;

    @Autowired
    ImUserService imUserService;

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

    @Override
    public ResponseVO getFriendRequest(String fromId, Integer appId) {

        QueryWrapper<ImFriendShipRequestEntity> query = new QueryWrapper();
        query.eq("app_id",appId);
        query.eq("from_id",fromId);
        List<String> userId = new ArrayList<>();

        List<ImFriendShipRequestEntity> requestList = imFriendShipRequestMapper.selectList(query);
        requestList.forEach(entity ->{
            userId.add(entity.getToId());
        });

        GetUserInfoReq getUserInfoReq = new GetUserInfoReq();
        getUserInfoReq.setAppId(getUserInfoReq.getAppId());
        getUserInfoReq.setUserIds(userId);
        ResponseVO<GetUserInfoResp> userInfo = imUserService.getUserInfo(getUserInfoReq);
        if(userInfo.getCode() != 200){
            throw new ApplicationException(UserErrorCode.SERVER_GET_USER_ERROR);
        }

        HashMap<String, ImUserDataEntity> userMap = new HashMap<>();
        List resp = new ArrayList();
        for (ImUserDataEntity user:
        userInfo.getData().getUserDataItem()) {
            userMap.put(user.getUserId(),user);
        }

        for (ImFriendShipRequestEntity entity : requestList) {
            GetFriendRequestResp respInfo = new GetFriendRequestResp();
            BeanUtils.copyProperties(entity,respInfo);
            if(userMap.containsKey(entity.getToId())){
                ImUserDataEntity imUserDataEntity = userMap.get(entity.getToId());
                respInfo.setPhoto(imUserDataEntity.getPhoto());
                respInfo.setNickName(imUserDataEntity.getNickName());
                respInfo.setUserSex(imUserDataEntity.getUserSex());
            }
            resp.add(respInfo);
        }

        return ResponseVO.successResponse(imFriendShipRequestMapper.selectList(query));
    }

    @Override
    public ResponseVO readAllFriendRequest(String fromId, Integer appId) {

        QueryWrapper<ImFriendShipRequestEntity> query = new QueryWrapper<>();
        query.eq("app_id",appId);
        query.eq("from_id",fromId);

        ImFriendShipRequestEntity update = new ImFriendShipRequestEntity();
        update.setReadStatus(1);
        imFriendShipRequestMapper.update(update,query);
        return ResponseVO.successResponse();
    }


}
