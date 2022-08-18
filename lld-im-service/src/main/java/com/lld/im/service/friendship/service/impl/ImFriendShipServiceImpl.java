package com.lld.im.service.friendship.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lld.im.codec.pack.AddFriendPack;
import com.lld.im.codec.pack.UpdateFriendPack;
import com.lld.im.common.BaseErrorCode;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.AllowFriendTypeEnum;
import com.lld.im.common.enums.FriendShipStatusEnum;
import com.lld.im.common.enums.command.FriendshipEventCommand;
import com.lld.im.common.model.RequestBase;
import com.lld.im.common.model.SyncJoinedResp;
import com.lld.im.common.model.SyncReq;
import com.lld.im.service.conversation.dao.ImConversationSetEntity;
import com.lld.im.service.friendship.dao.ImFriendShipEntity;
import com.lld.im.service.friendship.model.req.*;
import com.lld.im.service.friendship.model.resp.UpdateFriendshipResp;
import com.lld.im.service.message.service.MessageProducer;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipMapper;
import com.lld.im.common.enums.FriendShipErrorCode;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.service.friendship.model.resp.AddFriendResp;
import com.lld.im.service.friendship.service.ImFriendShipRequestService;
import com.lld.im.service.friendship.service.ImFriendShipService;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.service.seq.Seq;
import com.lld.im.service.utils.CallbackService;
import com.lld.im.service.utils.WriteUserSeq;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 09:23
 **/
@Service
public class ImFriendShipServiceImpl implements ImFriendShipService {

    @Autowired
    ImFriendShipMapper imFriendShipMapper;

    @Autowired
    ImUserService imUserService;

    @Autowired
    ImFriendShipService imFriendShipService;

    @Autowired
    ImFriendShipRequestService imFriendShipRequestService;

    @Autowired
    @Qualifier("redisSeq")
    Seq seq;

    @Autowired
    WriteUserSeq writeUserSeq;

    @Autowired
    CallbackService callbackService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    AppConfig appConfig;

    /**
     * @description 添加好友方法，需要判断用户是否开启好友验证，如果开启则插入好友申请记录，没有开启直接添加
     * @author chackylee
     * @date 2022/5/19 10:08
     * @param [req]
     * @return com.lld.im.common.ResponseVO
    */
    @Override
    public ResponseVO addFriend(AddFriendReq req) {

        List<AddFriendResp> result = new ArrayList<>();

        ResponseVO fromInfo = imUserService.getSingleUserInfo(req.getFromId(),req.getAppId());
        if (fromInfo.getCode() != BaseErrorCode.SUCCESS.getCode()) {
            return ResponseVO.errorResponse(fromInfo.getCode(),fromInfo.getMsg());
        }

        for (FriendDto dto : req.getAddFriendItems()) {
            AddFriendResp resp = new AddFriendResp();
            String toId = dto.getToId();
            ResponseVO<ImUserDataEntity> userInfo = imUserService.getSingleUserInfo(toId, req.getAppId());
            if (userInfo.getCode() != BaseErrorCode.SUCCESS.getCode()) {
                //不成功
                resp.setCode(userInfo.getCode());
                resp.setMsg(userInfo.getMsg());
                resp.setToId(toId);
                result.add(resp);
            } else {
                ImUserDataEntity data = userInfo.getData();
                if(data.getFriendAllowType() == AllowFriendTypeEnum.NEED.getCode()){
                    try {
                        ResponseVO addFriendRequest = imFriendShipRequestService.addFriendRequest(req.getFromId(), req.getAppId(), dto);
                        if(addFriendRequest.getCode() == BaseErrorCode.SUCCESS.getCode()){
                            resp.setCode(FriendShipErrorCode.ADD_FRIEND_NEED_VERIFY.getCode());
                            resp.setMsg(FriendShipErrorCode.ADD_FRIEND_NEED_VERIFY.getError());
                            resp.setToId(toId);
                            result.add(resp);
                        }else{
                            resp.setCode(addFriendRequest.getCode());
                            resp.setMsg(addFriendRequest.getMsg());
                            resp.setToId(toId);
                            result.add(resp);
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                        resp.setCode(BaseErrorCode.SYSTEM_ERROR.getCode());
                        resp.setMsg(BaseErrorCode.SYSTEM_ERROR.getError());
                        resp.setToId(toId);
                        result.add(resp);
                    }
                } else{
                    try {
                        ResponseVO doAddFriend = imFriendShipService.doAddFriend(req,req.getFromId(),dto);
                        resp.setCode(doAddFriend.getCode());
                        resp.setMsg(doAddFriend.getMsg());
                        resp.setToId(toId);
                        result.add(resp);
                    }catch (Exception e){
                        e.printStackTrace();
                        resp.setCode(BaseErrorCode.SYSTEM_ERROR.getCode());
                        resp.setMsg(BaseErrorCode.SYSTEM_ERROR.getError());
                        resp.setToId(toId);
                        result.add(resp);
                    }
                }
            }
        }

        //回调
        if(appConfig.isAddFriendCallback()){
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.AddFriend, JSONObject.toJSONString(result));
        }
        return ResponseVO.successResponse(result);
    }

    /**
     * @description 真正构建好友关系的方法
     * @author chackylee
     * @date 2022/5/19 10:07
     * @param [appId, fromId, dto]
     * @return com.lld.im.common.ResponseVO
    */
    @Transactional
    public ResponseVO doAddFriend(RequestBase requestBase, String fromId, FriendDto dto){

        QueryWrapper queryFrom = new QueryWrapper<>()
                .eq("from_id", fromId)
                .eq("app_id",requestBase.getAppId())
                .eq("to_id",dto.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(queryFrom);

        if(fromItem != null){
            if(fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode()){
                //重复添加
                return ResponseVO.errorResponse(FriendShipErrorCode.REPEAT_TO_ADD.getCode()
                        ,FriendShipErrorCode.REPEAT_TO_ADD.getError());
            } else{
                ImFriendShipEntity update = new ImFriendShipEntity();
                //将状态修改为正常，将设置备注，更新seq
                if(StringUtils.isNotEmpty(dto.getRemark())){
                    update.setRemark(dto.getRemark());
                }
                if(StringUtils.isNotEmpty(dto.getAddWording())){
                    update.setAddSource(dto.getAddSource());
                }

                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
                long seq = this.seq.getSeq(requestBase.getAppId() + Constants.SeqConstants.Friendship);
                update.setFriendSequence(seq);

                imFriendShipMapper.update(update,queryFrom);
                writeUserSeq.writeUserSeq(requestBase.getAppId(),fromId,Constants.SeqConstants.Friendship, seq);
            }
        }else {
            fromItem = new ImFriendShipEntity();
            fromItem.setAddSource(dto.getAddSource());
            fromItem.setAppId(requestBase.getAppId());
            fromItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
            fromItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            fromItem.setCreateTime(System.currentTimeMillis());
            fromItem.setRemark(dto.getRemark());
            fromItem.setToId(dto.getToId());
            fromItem.setFromId(fromId);
            long seq = this.seq.getSeq(requestBase.getAppId() + Constants.SeqConstants.Friendship);
            fromItem.setFriendSequence(seq);
            int insert = imFriendShipMapper.insert(fromItem);
            if(insert < 1){
                throw new ApplicationException(FriendShipErrorCode.FRIEND_ADD_ERROR);
            }
            writeUserSeq.writeUserSeq(requestBase.getAppId(),fromId,Constants.SeqConstants.Friendship, seq);
        }

        //判断to是否有添加from，如果没有，则插入数据
        QueryWrapper queryTo = new QueryWrapper<>()
                .eq("from_id", dto.getToId())
                .eq("app_id",requestBase.getAppId())
                .eq("to_id",fromId);
        ImFriendShipEntity toItem = imFriendShipMapper.selectOne(queryTo);

        if(toItem == null){
            toItem = new ImFriendShipEntity();
            toItem.setAppId(requestBase.getAppId());
            toItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getCode());
            toItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getCode());
            toItem.setCreateTime(System.currentTimeMillis());
            toItem.setToId(dto.getToId());
            toItem.setFromId(dto.getToId());
            toItem.setToId(fromId);
            long seq = this.seq.getSeq(requestBase.getAppId() + Constants.SeqConstants.Friendship);
            toItem.setFriendSequence(seq);
            int insert = imFriendShipMapper.insert(toItem);
            if(insert < 1){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                //添加失败
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_ADD_ERROR.getCode()
                        ,FriendShipErrorCode.FRIEND_ADD_ERROR.getError());
            }
            writeUserSeq.writeUserSeq(requestBase.getAppId(),dto.getToId(),Constants.SeqConstants.Friendship,seq);
        }

        AddFriendPack addFriendPack = new AddFriendPack();
        BeanUtils.copyProperties(fromItem,addFriendPack);
        if(requestBase != null){
            //TCP通知给from
            messageProducer.sendToUser(fromId,requestBase.getClientType(),requestBase.getImel(),
                    FriendshipEventCommand.FRIEND_ADD,addFriendPack,addFriendPack.getAppId());
        }else{
            messageProducer.sendToUser(fromId,null,null,
                    FriendshipEventCommand.FRIEND_ADD,addFriendPack,addFriendPack.getAppId());
        }

        //tcp通知给to
        AddFriendPack addToFriendPack = new AddFriendPack();
        BeanUtils.copyProperties(toItem,addToFriendPack);
        //TCP通知给from
        messageProducer.sendToUser(toItem.getToId(),null,null,
                FriendshipEventCommand.FRIEND_ADD,addToFriendPack,addToFriendPack.getAppId());

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO syncFriendShipList(SyncReq req) {

        if(req.getMaxLimit() > 100){
            req.setMaxLimit(100);
        }

        SyncJoinedResp resp = new SyncJoinedResp();

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("owner_id",req.getOperater());
        query.gt("sequence",req.getLastSequence());
        query.last(" limit " + req.getMaxLimit());
        List<ImFriendShipEntity> imConversationSetEntities = imFriendShipMapper.selectList(query);
//        List<ImGroupEntity> imGroupEntities = imGroupDataMapper.selectList(query);
        if(!CollectionUtil.isEmpty(imConversationSetEntities)){
            ImFriendShipEntity friend = imConversationSetEntities.get(imConversationSetEntities.size()-1);
            Long seq = imFriendShipMapper.getFriendShipMaxSeq(req.getAppId(),req.getOperater());
            resp.setCompleted(friend.getFriendSequence() >= seq);
            resp.setDataList(imConversationSetEntities);
            return ResponseVO.successResponse(resp);
        }

        return ResponseVO.successResponse();

    }

    /**
     * @description: 获取所有好友
     * @param
     * @return com.lld.im.common.ResponseVO
     * @author lld
     * @since 2022/7/9
     */
    @Override
    public ResponseVO getAllFriendShip(GetAllFriendShipReq req) {

        QueryWrapper query = new QueryWrapper<>()
                .eq("from_id", req.getFromId())
                .eq("app_id", req.getAppId());
        return ResponseVO.successResponse(imFriendShipMapper.selectList(query));
    }

    @Override
    public ResponseVO<ImFriendShipEntity> getRelation(GetRelationReq req) {

        QueryWrapper query = new QueryWrapper<>()
                .eq("from_id", req.getFromId())
                .eq("to_id", req.getToId())
                .eq("app_id", req.getAppId());
        ImFriendShipEntity imFriendShipEntity = imFriendShipMapper.selectOne(query);
        if(imFriendShipEntity == null){
            return ResponseVO.errorResponse(FriendShipErrorCode.REPEATSHIP_IS_NOT_EXIST.getCode()
                    ,FriendShipErrorCode.REPEATSHIP_IS_NOT_EXIST.getError());
        }
        return ResponseVO.successResponse(imFriendShipEntity);
    }

    @Override
    public ResponseVO<List<UpdateFriendshipResp>> updateFriendShip(UpdateFriendshipReq req) {

        List<UpdateFriendshipReq.UpdateItem> updateItems = req.getUpdateItems();

        if(updateItems.size() > 100){
            return ResponseVO.errorResponse(FriendShipErrorCode.UPDATE_FRIEND_SHIP_TO_LONG);
        }

        List<UpdateFriendshipResp> resp = new ArrayList<>();
        List<UpdateFriendPack> successItem = new ArrayList<>();

        updateItems.forEach(e -> {
            ResponseVO<ImFriendShipEntity> responseVO = doUpdateFriendship(e,req.getAppId());
            UpdateFriendshipResp updateFriendshipResp = new UpdateFriendshipResp();
            updateFriendshipResp.setCode(responseVO.getCode());
            updateFriendshipResp.setToId(e.getToId());
            resp.add(updateFriendshipResp);
            if(responseVO.isOk()){
                Long sequence = responseVO.getData().getFriendSequence();
                UpdateFriendPack updateItem = new UpdateFriendPack();
                updateItem.setCustomerItem(e.getCustomerItem());
                updateItem.setRemark(e.getRemark());
                updateItem.setToId(e.getToId());
                updateItem.setSequence(sequence);
                successItem.add(updateItem);
                messageProducer.sendToUser(req.getFromId(),req.getClientType(),
                        req.getImel(),FriendshipEventCommand.FRIEND_UPDATE,updateItem,req.getAppId());
                if (appConfig.isModifyFriendCallback()){
                    callbackService.callback(req.getAppId(),Constants.CallbackCommand.UpdateFriend,JSONObject.toJSONString(updateItem));
                }
            }
        });

        return ResponseVO.successResponse(resp);
    }

    public ResponseVO<ImFriendShipEntity> doUpdateFriendship(UpdateFriendshipReq.UpdateItem req,Integer appId) {

        long seq = this.seq.getSeq(appId + Constants.SeqConstants.Friendship);

        UpdateWrapper<ImFriendShipEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(ImFriendShipEntity::getRemark, req.getRemark()).set(ImFriendShipEntity::getFriendSequence,seq)
                .eq(ImFriendShipEntity::getAppId,appId).eq(ImFriendShipEntity::getToId,req.getToId());
        int update = imFriendShipMapper.update(null, updateWrapper);
        if(update == 1){
            ImFriendShipEntity resp = new ImFriendShipEntity();
            resp.setFriendSequence(seq);
            resp.setRemark(req.getRemark());
            resp.setToId(req.getToId());
            return ResponseVO.successResponse(resp);
        }
        return ResponseVO.errorResponse();
    }

    @Override
    public ResponseVO deleteFriend(DeleteFriendReq req) {

        QueryWrapper queryFrom = new QueryWrapper<>()
                .eq("from_id", req.getFromId())
                .eq("app_id", req.getAppId())
                .eq("to_id", req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(queryFrom);
        if (fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_DELETED.getCode()) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_IS_DELETED);
        }
//        queryFrom
        ImFriendShipEntity update = new ImFriendShipEntity();
        update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETED.getCode());
        imFriendShipMapper.update(update, queryFrom);

        //TODO 发送消息给同步端
//        messageProducer.sendToUser(req.getFromId(),req.getClientType(),req.getImel(),FriendshipEventCommand.FRIEND_DELETE,
//                "",req.getAppId());

        //回调
        if(appConfig.isDeleteFriendCallback()){
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.DeleteFriend, JSONObject.toJSONString(req));
        }
        return ResponseVO.successResponse();
    }
}
