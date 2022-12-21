package com.lld.im.service.friendship.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lld.im.codec.pack.friendship.ApproverFriendRequestPack;
import com.lld.im.codec.pack.friendship.ReadAllFriendRequestPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ApproverFriendRequestStatusEnum;
import com.lld.im.common.enums.UserErrorCode;
import com.lld.im.common.enums.command.FriendshipEventCommand;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.common.model.SyncReq;
import com.lld.im.common.model.SyncResp;
import com.lld.im.service.friendship.dao.ImFriendShipRequestEntity;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipRequestMapper;
import com.lld.im.common.enums.FriendShipErrorCode;
import com.lld.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.lld.im.service.friendship.model.req.FriendDto;
import com.lld.im.service.friendship.model.req.ReadFriendShipRequestReq;
import com.lld.im.service.friendship.model.resp.GetFriendRequestResp;
import com.lld.im.service.friendship.service.ImFriendShipRequestService;
import com.lld.im.service.friendship.service.ImFriendShipService;
import com.lld.im.service.message.service.MessageProducer;
import com.lld.im.service.service.seq.Seq;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.model.req.GetUserInfoReq;
import com.lld.im.service.user.model.resp.GetUserInfoResp;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.utils.WriteUserSeq;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    @Autowired
    @Qualifier("redisSeq")
    Seq seq;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    ImFriendShipService imFriendShipService;

    @Autowired
    WriteUserSeq writeUserSeq;

    @Override
    @Transactional
    public ResponseVO addFriendRequest(String fromId, Integer appId, FriendDto dto) {

        QueryWrapper query = new QueryWrapper();
        query.eq("from_id", fromId);
        query.eq("to_id", dto.getToId());
        query.eq("app_id", appId);
        ImFriendShipRequestEntity request = imFriendShipRequestMapper.selectOne(query);

        long seq = this.seq.getSeq(appId + Constants.SeqConstants.FriendshipRequest);

        if (request == null) {
            request = new ImFriendShipRequestEntity();
            request.setAddWording(dto.getAddWording());
            request.setSequence(seq);
            request.setAddSource(dto.getAddSource());
            request.setAppId(appId);
            request.setCreateTime(System.currentTimeMillis());
            request.setFromId(fromId);
            request.setReadStatus(0);
            request.setApproveStatus(0);
            request.setRemark(dto.getRemark());
            request.setToId(dto.getToId());
            int insert = imFriendShipRequestMapper.insert(request);
            if (insert < 0) {
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_REQUEST_ERROR.getCode(),
                        FriendShipErrorCode.ADD_FRIEND_REQUEST_ERROR.getError());
            }
        } else {
            request.setSequence(seq);
            request.setReadStatus(0);
            request.setUpdateTime(System.currentTimeMillis());
            request.setAddWording(dto.getAddWording());
            int i = imFriendShipRequestMapper.updateById(request);
            if (i != 1) {
                return ResponseVO.errorResponse(FriendShipErrorCode.ADD_FRIEND_REQUEST_ERROR.getCode(),
                        FriendShipErrorCode.ADD_FRIEND_REQUEST_ERROR.getError());
            }
        }

        writeUserSeq.writeUserSeq(request.getAppId(),
                request.getToId(),Constants.SeqConstants.FriendshipRequest,
                seq);

        //发送好友申请的tcp给接收方
        messageProducer.sendToUser(dto.getToId(), null, "", FriendshipEventCommand.FRIEND_REQUEST,
                request, appId);
        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO getFriendRequest(String fromId, Integer appId) {

        QueryWrapper<ImFriendShipRequestEntity> query = new QueryWrapper();
        query.eq("app_id", appId);
        query.eq("from_id", fromId);
        List<String> userId = new ArrayList<>();

        List<ImFriendShipRequestEntity> requestList = imFriendShipRequestMapper.selectList(query);
        requestList.forEach(entity -> {
            userId.add(entity.getToId());
        });

        GetUserInfoReq getUserInfoReq = new GetUserInfoReq();
        getUserInfoReq.setAppId(getUserInfoReq.getAppId());
        getUserInfoReq.setUserIds(userId);
        ResponseVO<GetUserInfoResp> userInfo = imUserService.getUserInfo(getUserInfoReq);
        if (userInfo.getCode() != 200) {
            throw new ApplicationException(UserErrorCode.SERVER_GET_USER_ERROR);
        }

        HashMap<String, ImUserDataEntity> userMap = new HashMap<>();
        List resp = new ArrayList();
        for (ImUserDataEntity user :
                userInfo.getData().getUserDataItem()) {
            userMap.put(user.getUserId(), user);
        }

        for (ImFriendShipRequestEntity entity : requestList) {
            GetFriendRequestResp respInfo = new GetFriendRequestResp();
            BeanUtils.copyProperties(entity, respInfo);
            if (userMap.containsKey(entity.getToId())) {
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
    public ResponseVO readAllFriendRequest(ReadFriendShipRequestReq req) {

        QueryWrapper<ImFriendShipRequestEntity> query = new QueryWrapper<>();
        query.eq("app_id", req.getAppId());
        query.eq("to_id", req.getFromId());

        ImFriendShipRequestEntity update = new ImFriendShipRequestEntity();
        long seq = this.seq.getSeq(req.getAppId() + Constants.SeqConstants.FriendshipRequest);
        update.setReadStatus(1);
        update.setSequence(seq);
        imFriendShipRequestMapper.update(update, query);
        writeUserSeq.writeUserSeq(req.getAppId(),
                req.getFromId(),Constants.SeqConstants.FriendshipRequest,
                seq);
        ReadAllFriendRequestPack readAllFriendRequestPack = new ReadAllFriendRequestPack();
        readAllFriendRequestPack.setFromId(req.getFromId());
        readAllFriendRequestPack.setSequence(seq);
        //TCP通知
        messageProducer.sendToUser(req.getFromId(),req.getClientType(),req.getImei(),FriendshipEventCommand
                .FRIEND_REQUEST_READ,readAllFriendRequestPack,req.getAppId());

        return ResponseVO.successResponse();
    }

    /**
     * @description 审批好友请求 1同意 2拒绝
     * @author chackylee
     * @date 2022/8/3 14:09
     * @param
     * @return com.lld.im.common.ResponseVO
    */
    @Override
    @Transactional
    public ResponseVO approverFriendRequest(ApproveFriendRequestReq req) {

        ImFriendShipRequestEntity imFriendShipRequestEntity = imFriendShipRequestMapper.selectById(req.getId());
        if(imFriendShipRequestEntity == null){
            throw new ApplicationException(FriendShipErrorCode.FRIEND_REQUEST_IS_NOT_EXIST);
        }

        if(!req.getOperater().equals(imFriendShipRequestEntity.getToId())){
            //只能审批发给自己的好友请求
            throw new ApplicationException(FriendShipErrorCode.NOT_APPROVER_OTHER_MAN_REQUEST);
        }

        ImFriendShipRequestEntity update = new ImFriendShipRequestEntity();
        long seq = this.seq.getSeq(req.getAppId() + Constants.SeqConstants.FriendshipRequest);
        update.setSequence(seq);
        update.setApproveStatus(req.getStatus());
        update.setUpdateTime(System.currentTimeMillis());
        update.setId(req.getId());
        imFriendShipRequestMapper.updateById(update);

        writeUserSeq.writeUserSeq(req.getAppId(),
                req.getOperater(),Constants.SeqConstants.FriendshipRequest,
                seq);

        if(ApproverFriendRequestStatusEnum.AGREE.getCode() == req.getStatus()){
            //同意 ===> 去执行添加好友逻辑
            FriendDto dto = new FriendDto();
            dto.setAddSource(imFriendShipRequestEntity.getAddSource());
            dto.setAddWording(imFriendShipRequestEntity.getAddWording());
            dto.setRemark(imFriendShipRequestEntity.getRemark());
            dto.setToId(imFriendShipRequestEntity.getToId());
            ResponseVO responseVO = imFriendShipService.doAddFriend(req, imFriendShipRequestEntity.getFromId(), dto);
//            if(!responseVO.isOk()){
////                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
//                return responseVO;
//            }
            if(!responseVO.isOk() && responseVO.getCode() != FriendShipErrorCode.REPEAT_TO_ADD.getCode()){
                return responseVO;
            }
        }

        ApproverFriendRequestPack approverFriendRequestPack = new ApproverFriendRequestPack();
        approverFriendRequestPack.setId(req.getId());
        approverFriendRequestPack.setSequence(seq);
        approverFriendRequestPack.setStatus(req.getStatus());
        messageProducer.sendToUser(imFriendShipRequestEntity.getToId(),req.getClientType(),req.getImei(),FriendshipEventCommand
        .FRIEND_REQUEST_APPROVER,approverFriendRequestPack,req.getAppId());

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO syncFriendShipRequest(SyncReq req) {
        if(req.getMaxLimit() > 100){
            req.setMaxLimit(100);
        }

        SyncResp resp = new SyncResp();

        QueryWrapper<ImFriendShipRequestEntity> query = new QueryWrapper<>();
        query.eq("to_id",req.getOperater());
        query.gt("sequence",req.getLastSequence());
        query.last(" limit " + req.getMaxLimit());
        query.orderByAsc("sequence");
        List<ImFriendShipRequestEntity> list = imFriendShipRequestMapper.selectList(query);
//        List<ImGroupEntity> imGroupEntities = imGroupDataMapper.selectList(query);
        if(!CollectionUtil.isEmpty(list)){
            ImFriendShipRequestEntity friend = list.get(list.size() - 1);
            Long seq = imFriendShipRequestMapper.getFriendShipRequestMaxSeq(req.getAppId(),req.getOperater());
            resp.setCompleted(friend.getSequence() >= seq);
            resp.setDataList(list);
            resp.setMaxSequence(seq);
            return ResponseVO.successResponse(resp);
        }
        resp.setCompleted(true);
        return ResponseVO.successResponse(resp);
    }


}
