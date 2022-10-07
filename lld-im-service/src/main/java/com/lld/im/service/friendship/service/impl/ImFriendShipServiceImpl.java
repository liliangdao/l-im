package com.lld.im.service.friendship.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.jeffreyning.mybatisplus.base.MppBaseMapper;
import com.github.jeffreyning.mybatisplus.service.MppServiceImpl;
import com.lld.im.codec.pack.AddFriendBlackPack;
import com.lld.im.codec.pack.AddFriendPack;
import com.lld.im.codec.pack.DeleteFriendPack;
import com.lld.im.codec.pack.UpdateFriendPack;
import com.lld.im.common.BaseErrorCode;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.AllowFriendTypeEnum;
import com.lld.im.common.enums.CheckFriendShipTypeEnum;
import com.lld.im.common.enums.FriendShipStatusEnum;
import com.lld.im.common.enums.command.FriendshipEventCommand;
import com.lld.im.common.model.RequestBase;
import com.lld.im.common.model.SyncReq;
import com.lld.im.common.model.SyncResp;
import com.lld.im.service.friendship.dao.ImFriendShipEntity;
import com.lld.im.service.friendship.model.req.*;
import com.lld.im.service.friendship.model.resp.AddFriendShipResp;
import com.lld.im.service.friendship.model.resp.CheckFriendShipResp;
import com.lld.im.service.friendship.model.resp.ImportFriendShipResp;
import com.lld.im.service.friendship.model.resp.UpdateFriendshipResp;
import com.lld.im.service.message.service.MessageProducer;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.friendship.dao.mapper.ImFriendShipMapper;
import com.lld.im.common.enums.FriendShipErrorCode;
import com.lld.im.common.exception.ApplicationException;
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
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 09:23
 **/
@Service
public class ImFriendShipServiceImpl extends
        MppServiceImpl<ImFriendShipMapper, ImFriendShipEntity> implements ImFriendShipService {

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

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * @param
     * @return com.lld.im.common.ResponseVO
     * @description: 导入好友
     * @author lld
     * @since 2022-10-02
     */
    @Override
    public ResponseVO importFriendShip(ImportFriendShipReq req) {

        if (req.getFriendItem().size() > 100) {
            return ResponseVO.errorResponse(FriendShipErrorCode
                    .FRIEND_SHIP_IMPORT_SIZE_TO_LONG);
        }
        ImportFriendShipResp importFriendShipResp = new ImportFriendShipResp();
        List<String> errorId = new ArrayList<>();
        List<String> successId = new ArrayList<>();

        for (ImportFriendShipReq.ImportFriendDto dto : req.getFriendItem()) {
            if (StringUtils.isBlank(dto.getToId())) {
                errorId.add(dto.getToId());
                continue;
            }
            ImFriendShipEntity entity = new ImFriendShipEntity();
            entity.setFromId(req.getFromId());
            BeanUtils.copyProperties(dto, entity);
            entity.setAppId(req.getAppId());
            try {
                int insert = imFriendShipMapper.insert(entity);
                if (insert == 1) {
                    successId.add(dto.getToId());
                } else {
                    errorId.add(dto.getToId());
                }
            } catch (Exception e) {
                e.printStackTrace();
                errorId.add(dto.getToId());
            }
        }

        importFriendShipResp.setSuccessId(successId);
        importFriendShipResp.setErrorId(errorId);
        return ResponseVO.successResponse(importFriendShipResp);
    }

    /**
     * @param [req]
     * @return com.lld.im.common.ResponseVO
     * @description 添加好友方法，需要判断用户是否开启好友验证，如果开启则插入好友申请记录，没有开启直接添加
     * @author chackylee
     * @date 2022/5/19 10:08
     */
    @Override
    public ResponseVO addFriend(AddFriendShipReq req) {

        List<AddFriendShipResp> result = new ArrayList<>();

        ResponseVO fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (fromInfo.getCode() != BaseErrorCode.SUCCESS.getCode()) {
            return ResponseVO.errorResponse(fromInfo.getCode(), fromInfo.getMsg());
        }

        for (FriendDto dto : req.getAddItems()) {
            AddFriendShipResp resp = new AddFriendShipResp();
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
                if (data.getFriendAllowType() == AllowFriendTypeEnum.NEED.getCode()) {
                    try {
                        ResponseVO addFriendRequest = imFriendShipRequestService.addFriendRequest(req.getFromId(), req.getAppId(), dto);
                        if (addFriendRequest.getCode() == BaseErrorCode.SUCCESS.getCode()) {
                            resp.setCode(FriendShipErrorCode.ADD_FRIEND_NEED_VERIFY.getCode());
                            resp.setMsg(FriendShipErrorCode.ADD_FRIEND_NEED_VERIFY.getError());
                            resp.setToId(toId);
                            result.add(resp);
                        } else {
                            resp.setCode(addFriendRequest.getCode());
                            resp.setMsg(addFriendRequest.getMsg());
                            resp.setToId(toId);
                            result.add(resp);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        resp.setCode(BaseErrorCode.SYSTEM_ERROR.getCode());
                        resp.setMsg(BaseErrorCode.SYSTEM_ERROR.getError());
                        resp.setToId(toId);
                        result.add(resp);
                    }
                } else {
                    try {
                        ResponseVO doAddFriend = imFriendShipService.doAddFriend(req, req.getFromId(), dto);
                        resp.setCode(doAddFriend.getCode());
                        resp.setMsg(doAddFriend.getMsg());
                        resp.setToId(toId);
                        result.add(resp);
                    } catch (Exception e) {
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
        if (appConfig.isAddFriendCallback()) {
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.AddFriend, JSONObject.toJSONString(result));
        }
        return ResponseVO.successResponse(result);
    }

    /**
     * @param [appId, fromId, dto]
     * @return com.lld.im.common.ResponseVO
     * @description 真正构建好友关系的方法
     * @author chackylee
     * @date 2022/5/19 10:07
     */
    @Transactional
    public ResponseVO doAddFriend(RequestBase requestBase, String fromId, FriendDto dto) {

        QueryWrapper queryFrom = new QueryWrapper<>()
                .eq("from_id", fromId)
                .eq("app_id", requestBase.getAppId())
                .eq("to_id", dto.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(queryFrom);

        if (fromItem != null) {
            if (fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getStatus()) {
                //重复添加
                return ResponseVO.errorResponse(FriendShipErrorCode.REPEAT_TO_ADD.getCode()
                        , FriendShipErrorCode.REPEAT_TO_ADD.getError());
            } else {
                ImFriendShipEntity update = new ImFriendShipEntity();
                //将状态修改为正常，将设置备注，更新seq
                if (StringUtils.isNotEmpty(dto.getRemark())) {
                    update.setRemark(dto.getRemark());
                }
                if (StringUtils.isNotEmpty(dto.getAddWording())) {
                    update.setAddSource(dto.getAddSource());
                }

                update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getStatus());
                long seq = this.seq.getSeq(requestBase.getAppId() + ":" + Constants.SeqConstants.Friendship);
                update.setFriendSequence(seq);

                imFriendShipMapper.update(update, queryFrom);
                writeUserSeq.writeUserSeq(requestBase.getAppId(), fromId, Constants.SeqConstants.Friendship, seq);
            }
        } else {
            fromItem = new ImFriendShipEntity();
            fromItem.setAddSource(dto.getAddSource());
            fromItem.setAppId(requestBase.getAppId());
            fromItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getStatus());
            fromItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getStatus());
            fromItem.setCreateTime(System.currentTimeMillis());
            fromItem.setRemark(dto.getRemark());
            fromItem.setToId(dto.getToId());
            fromItem.setFromId(fromId);
            long seq = this.seq.getSeq(requestBase.getAppId() + ":" + Constants.SeqConstants.Friendship);
            fromItem.setFriendSequence(seq);
            int insert = imFriendShipMapper.insert(fromItem);
            if (insert < 1) {
                throw new ApplicationException(FriendShipErrorCode.FRIEND_ADD_ERROR);
            }
            writeUserSeq.writeUserSeq(requestBase.getAppId(), fromId, Constants.SeqConstants.Friendship, seq);
        }

        //判断to是否有添加from，如果没有，则插入数据
        QueryWrapper queryTo = new QueryWrapper<>()
                .eq("from_id", dto.getToId())
                .eq("app_id", requestBase.getAppId())
                .eq("to_id", fromId);
        ImFriendShipEntity toItem = imFriendShipMapper.selectOne(queryTo);

        if (toItem == null) {
            toItem = new ImFriendShipEntity();
            toItem.setAppId(requestBase.getAppId());
            toItem.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getStatus());
            toItem.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NORMAL.getStatus());
            toItem.setCreateTime(System.currentTimeMillis());
            toItem.setToId(dto.getToId());
            toItem.setFromId(dto.getToId());
            toItem.setToId(fromId);
            long seq = this.seq.getSeq(requestBase.getAppId() + ":" + Constants.SeqConstants.Friendship);
            toItem.setFriendSequence(seq);
            int insert = imFriendShipMapper.insert(toItem);
            if (insert < 1) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                //添加失败
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_ADD_ERROR.getCode()
                        , FriendShipErrorCode.FRIEND_ADD_ERROR.getError());
            }
            writeUserSeq.writeUserSeq(requestBase.getAppId(), dto.getToId(), Constants.SeqConstants.Friendship, seq);
        }


        AddFriendPack addFriendPack = new AddFriendPack();
        BeanUtils.copyProperties(fromItem, addFriendPack);
        if (requestBase != null) {
            //TCP通知给from
            messageProducer.sendToUser(fromId, requestBase.getClientType(), requestBase.getImel(),
                    FriendshipEventCommand.FRIEND_ADD, addFriendPack, requestBase.getAppId());
        } else {
            messageProducer.sendToUser(fromId,
                    FriendshipEventCommand.FRIEND_ADD, addFriendPack, requestBase.getAppId());
        }

        //tcp通知给to
        AddFriendPack addToFriendPack = new AddFriendPack();
        BeanUtils.copyProperties(toItem, addToFriendPack);
        //TCP通知给from
        messageProducer.sendToUser(toItem.getToId(),
                FriendshipEventCommand.FRIEND_ADD, addToFriendPack, requestBase.getAppId());

        this.modifyFirendListForCache(dto.getToId(), fromId, requestBase.getAppId(), "ADD");
        return ResponseVO.successResponse();
    }

    /**
     * @param [req]
     * @return com.lld.im.common.ResponseVO
     * @description 同步好友列表接口，限客户端调用
     * @author chackylee
     * @date 2022/8/23 10:15
     */
    @Override
    public ResponseVO syncFriendShipList(SyncReq req) {

        if (req.getMaxLimit() > 100) {
            req.setMaxLimit(100);
        }

        SyncResp resp = new SyncResp();

        QueryWrapper<ImFriendShipEntity> query = new QueryWrapper<>();
        query.eq("from_id", req.getOperater());
        query.gt("friend_sequence", req.getLastSequence());
        query.last(" limit " + req.getMaxLimit());
        query.orderByAsc("friend_sequence");
        List<ImFriendShipEntity> imConversationSetEntities = imFriendShipMapper.selectList(query);
//        List<ImGroupEntity> imGroupEntities = imGroupDataMapper.selectList(query);
        if (!CollectionUtil.isEmpty(imConversationSetEntities)) {
            ImFriendShipEntity friend = imConversationSetEntities.get(imConversationSetEntities.size() - 1);
            Long seq = imFriendShipMapper.getFriendShipMaxSeq(req.getAppId(), req.getOperater());
            resp.setCompleted(friend.getFriendSequence() >= seq);
            resp.setDataList(imConversationSetEntities);
            resp.setMaxSequence(seq);
            return ResponseVO.successResponse(resp);
        }

        resp.setCompleted(true);
        return ResponseVO.successResponse(resp);

    }

    /**
     * @param
     * @return com.lld.im.common.ResponseVO
     * @description: 获取所有好友
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
        if (imFriendShipEntity == null) {
            return ResponseVO.errorResponse(FriendShipErrorCode.REPEATSHIP_IS_NOT_EXIST.getCode()
                    , FriendShipErrorCode.REPEATSHIP_IS_NOT_EXIST.getError());
        }
        return ResponseVO.successResponse(imFriendShipEntity);
    }

    /**
     * @param [req]
     * @return com.lld.im.common.ResponseVO<java.util.List < com.lld.im.service.friendship.model.resp.UpdateFriendshipResp>>
     * @description 更新好友
     * @author chackylee
     * @date 2022/7/9
     */
    @Override
    public ResponseVO<List<UpdateFriendshipResp>> updateFriendShip(UpdateFriendshipReq req) {

        List<UpdateFriendshipReq.UpdateItem> updateItems = req.getUpdateItems();

        if (updateItems.size() > 100) {
            return ResponseVO.errorResponse(FriendShipErrorCode.UPDATE_FRIEND_SHIP_TO_LONG);
        }

        List<UpdateFriendshipResp> resp = new ArrayList<>();
        List<UpdateFriendPack> successItem = new ArrayList<>();

        updateItems.forEach(e -> {
            ResponseVO<ImFriendShipEntity> responseVO = doUpdateFriendship(e, req.getAppId());
            UpdateFriendshipResp updateFriendshipResp = new UpdateFriendshipResp();
            updateFriendshipResp.setCode(responseVO.getCode());
            updateFriendshipResp.setToId(e.getToId());
            resp.add(updateFriendshipResp);
            if (responseVO.isOk()) {
                Long sequence = responseVO.getData().getFriendSequence();
                UpdateFriendPack updateItem = new UpdateFriendPack();
                updateItem.setCustomerItem(e.getCustomerItem());
                updateItem.setRemark(e.getRemark());
                updateItem.setToId(e.getToId());
                updateItem.setSequence(sequence);
                successItem.add(updateItem);
                messageProducer.sendToUser(req.getFromId(), req.getClientType(),
                        req.getImel(), FriendshipEventCommand.FRIEND_UPDATE, updateItem, req.getAppId());
                if (appConfig.isModifyFriendCallback()) {
                    callbackService.callback(req.getAppId(), Constants.CallbackCommand.UpdateFriend, JSONObject.toJSONString(updateItem));
                }
            }
        });

        return ResponseVO.successResponse(resp);
    }

    /**
     * @param [req]
     * @return com.lld.im.common.ResponseVO
     * @description 校验好友，支持单方校验和双方校验。不同的校验返回的status不同
     * @author chackylee
     * @date 2022/8/22 10:14
     */
    @Override
    public ResponseVO checkFriend(CheckFriendShipReq req) {

        Map<String, Integer> result
                = req.getToIds().stream()
                .collect(Collectors.toMap(Function.identity(), s -> 0));

        if (req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            List<CheckFriendShipResp> sigleCheck = imFriendShipMapper.checkFriendShip(req);
            Map<String, Integer> collect = sigleCheck.stream()
                    .collect(Collectors.toMap(CheckFriendShipResp::getToId
                            , CheckFriendShipResp::getStatus));
            for (String toId : result.keySet()) {
                if (!collect.containsKey(toId)) {
                    CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                    checkFriendShipResp.setFromId(req.getFromId());
                    checkFriendShipResp.setStatus(result.get(toId));
                    checkFriendShipResp.setToId(toId);
                    sigleCheck.add(checkFriendShipResp);
                }
            }
            return ResponseVO.successResponse(sigleCheck);
        } else {
            List<CheckFriendShipResp> checkFriendShipResps = imFriendShipMapper.checkFriendShipBoth(req);
            Map<String, Integer> collect = checkFriendShipResps.stream()
                    .collect(Collectors.toMap(CheckFriendShipResp::getToId
                            , CheckFriendShipResp::getStatus));
            for (String toId : result.keySet()) {
                if (!collect.containsKey(toId)) {
                    CheckFriendShipResp checkFriendShipResp = new CheckFriendShipResp();
                    checkFriendShipResp.setFromId(req.getFromId());
                    checkFriendShipResp.setStatus(result.get(toId));
                    checkFriendShipResp.setToId(toId);
                    checkFriendShipResps.add(checkFriendShipResp);
                }
            }
            return ResponseVO.successResponse(checkFriendShipResps);
        }
    }

    @Override
    public ResponseVO addBlack(AddFriendShipBlackReq req) {
        List<AddFriendShipResp> result = new ArrayList<>();

        ResponseVO fromInfo = imUserService.getSingleUserInfo(req.getFromId(), req.getAppId());
        if (fromInfo.getCode() != BaseErrorCode.SUCCESS.getCode()) {
            return ResponseVO.errorResponse(fromInfo.getCode(), fromInfo.getMsg());
        }

        for (String toId : req.getAddItems()) {
            AddFriendShipResp resp = new AddFriendShipResp();
            ResponseVO<ImUserDataEntity> userInfo = imUserService.getSingleUserInfo(toId, req.getAppId());
            if (userInfo.getCode() != BaseErrorCode.SUCCESS.getCode()) {
                //不成功
                resp.setCode(userInfo.getCode());
                resp.setMsg(userInfo.getMsg());
                resp.setToId(toId);
                result.add(resp);
            } else {
                ImFriendShipEntity entity = new ImFriendShipEntity();
                entity.setFromId(req.getFromId());
                entity.setToId(toId);
                entity.setAppId(req.getAppId());
                entity.setBlack(FriendShipStatusEnum.BLACK_STATUS_BLACKED.getStatus());
                long seq = this.seq.getSeq(req.getAppId() + ":" + Constants.SeqConstants.FriendshipBlack);
                entity.setBlackSequence(seq);

                final ImFriendShipEntity imFriendShipEntity = imFriendShipMapper.selectByMultiId(entity);
                if (imFriendShipEntity == null) {
                    entity.setCreateTime(System.currentTimeMillis());
                    entity.setStatus(FriendShipStatusEnum.FRIEND_STATUS_NO_FRIEND.getStatus());
                }
                boolean b = this.saveOrUpdateByMultiId(entity);
                if (b) {
                    resp.setCode(0);
                    resp.setMsg("");
                    resp.setToId(toId);
                    result.add(resp);
                    AddFriendBlackPack addFriendBlackPack = new AddFriendBlackPack();
                    addFriendBlackPack.setFromId(req.getFromId());
                    addFriendBlackPack.setToId(toId);
                    addFriendBlackPack.setSequence(seq);
                    writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.FriendshipBlack, seq);
                    //发送tcp通知
                    messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImel(),
                            FriendshipEventCommand.FRIEND_BLACK_ADD, addFriendBlackPack, req.getAppId());
                } else {
                    resp.setCode(500);
                    resp.setMsg("error");
                    resp.setToId(toId);
                    result.add(resp);
                }
            }
        }

        //回调
        if (appConfig.isAddFriendShipBlackCallback()) {
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.AddBlack, JSONObject.toJSONString(result));
        }
        return ResponseVO.successResponse(result);
    }

    /**
     * @param [req]
     * @return com.lld.im.common.ResponseVO
     * @description 删除黑名单
     * @author chackylee
     * @date 2022/8/23 10:20
     */
    @Override
    public ResponseVO deleteBlack(DeleteBlackReq req) {

        QueryWrapper queryFrom = new QueryWrapper<>()
                .eq("from_id", req.getFromId())
                .eq("app_id", req.getAppId())
                .eq("to_id", req.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(queryFrom);
        if (fromItem.getStatus() == FriendShipStatusEnum.BLACK_STATUS_BLACKED.getStatus()) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_IS_BLACK);
        }

        ImFriendShipEntity update = new ImFriendShipEntity();
        long seq = this.seq.getSeq(req.getFromId() + ":" + Constants.SeqConstants.Friendship);
        update.setBlackSequence(seq);
        update.setBlack(FriendShipStatusEnum.BLACK_STATUS_NORMAL.getStatus());
        imFriendShipMapper.update(update, queryFrom);

        DeleteFriendPack deleteFriendPack = new DeleteFriendPack();
        deleteFriendPack.setFromId(req.getFromId());
        deleteFriendPack.setToId(req.getToId());

        writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.FriendshipBlack, seq);

        messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImel(), FriendshipEventCommand.FRIEND_BLACK_DELETE,
                deleteFriendPack, req.getAppId());

        //回调
        if (appConfig.isDeleteFriendShipBlackCallback()) {
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.DeleteBlack, JSONObject.toJSONString(req));
        }
        return ResponseVO.successResponse();

    }

    @Override
    public ResponseVO checkBlck(CheckFriendShipReq req) {

        if (req.getCheckType() == CheckFriendShipTypeEnum.SINGLE.getType()) {
            List<CheckFriendShipResp> sigleCheck = imFriendShipMapper.checkFriendShipBlack(req);
            return ResponseVO.successResponse(sigleCheck);
        } else {
            return ResponseVO.successResponse(imFriendShipMapper.checkFriendShipBlackBoth(req));
        }

    }

    /**
     * @param
     * @return com.lld.im.common.ResponseVO
     * @description: 获取所有好友id列表
     * @author lld
     * @since 2022/9/25
     */
    @Override
    public List<String> getAllFriendId(String userId, Integer appId) {

        String redisKey = appId + ":" + Constants.RedisConstants.friendList + ":" + userId;
        if (stringRedisTemplate.hasKey(redisKey)) {
            return new ArrayList<>(stringRedisTemplate.opsForSet()
                    .members(redisKey));
        }

        List<String> allFriendId = imFriendShipMapper.getAllFriendId(userId, appId);
        String[] objects = allFriendId.toArray(new String[allFriendId.size()]);
        stringRedisTemplate.execute((RedisConnection redisConnection) -> {
            stringRedisTemplate.opsForSet()
                    .add(redisKey, objects);
            stringRedisTemplate.expire(redisKey,
                    5, TimeUnit.MINUTES);
            return null;
        });

        return allFriendId;
    }

    private void modifyFirendListForCache(String friendId, String userId, Integer appId, String operate) {
        String redisKey = appId + ":" + Constants.RedisConstants.friendList + ":" + userId;

        stringRedisTemplate.execute((RedisConnection redisConnection) -> {
            if (!stringRedisTemplate.hasKey(redisKey)) {
                return null;
            }
            if (operate.equals("ADD")) {
                stringRedisTemplate.opsForSet()
                        .add(redisKey, friendId);
            } else if (operate.equals("REMOVE")) {
                stringRedisTemplate.opsForSet()
                        .remove(redisKey, friendId);
            }
            return null;
        });
    }

    /**
     * @param [req, appId]
     * @return com.lld.im.common.ResponseVO<com.lld.im.service.friendship.dao.ImFriendShipEntity>
     * @description 真正更新好友的方法
     * @author chackylee
     */
    public ResponseVO<ImFriendShipEntity> doUpdateFriendship(UpdateFriendshipReq.UpdateItem req, Integer appId) {

        long seq = this.seq.getSeq(appId + ":" + Constants.SeqConstants.Friendship);

        UpdateWrapper<ImFriendShipEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().set(ImFriendShipEntity::getRemark, req.getRemark()).set(ImFriendShipEntity::getFriendSequence, seq)
                .eq(ImFriendShipEntity::getAppId, appId).eq(ImFriendShipEntity::getToId, req.getToId());
        int update = imFriendShipMapper.update(null, updateWrapper);
        if (update == 1) {
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
        if (fromItem.getStatus() == FriendShipStatusEnum.FRIEND_STATUS_DELETED.getStatus()) {
            throw new ApplicationException(FriendShipErrorCode.FRIEND_IS_DELETED);
        }
//        queryFrom
        ImFriendShipEntity update = new ImFriendShipEntity();
        long seq = this.seq.getSeq(req.getFromId() + ":" + Constants.SeqConstants.Friendship);
        update.setFriendSequence(seq);
        update.setStatus(FriendShipStatusEnum.FRIEND_STATUS_DELETED.getStatus());
        imFriendShipMapper.update(update, queryFrom);

        DeleteFriendPack deleteFriendPack = new DeleteFriendPack();
        deleteFriendPack.setFromId(req.getFromId());
        deleteFriendPack.setToId(req.getToId());

        writeUserSeq.writeUserSeq(req.getAppId(), req.getFromId(), Constants.SeqConstants.FriendshipBlack, seq);

        messageProducer.sendToUser(req.getFromId(), req.getClientType(), req.getImel(), FriendshipEventCommand.FRIEND_DELETE,
                deleteFriendPack, req.getAppId());

        //回调
        if (appConfig.isDeleteFriendCallback()) {
            callbackService.callback(req.getAppId(), Constants.CallbackCommand.DeleteFriend, JSONObject.toJSONString(req));
        }

        return ResponseVO.successResponse();
    }
}
