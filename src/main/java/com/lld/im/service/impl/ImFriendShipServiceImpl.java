package com.lld.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lld.im.common.BaseErrorCode;
import com.lld.im.common.ResponseVO;
import com.lld.im.dao.ImFriendShipEntity;
import com.lld.im.dao.ImUserDataEntity;
import com.lld.im.dao.mapper.ImFriendShipMapper;
import com.lld.im.enums.FriendShipEnum;
import com.lld.im.enums.FriendShipErrorCode;
import com.lld.im.exception.ApplicationException;
import com.lld.im.model.req.friendship.AddFriendReq;
import com.lld.im.model.req.friendship.DeleteFriendReq;
import com.lld.im.model.req.friendship.FriendDto;
import com.lld.im.model.resp.friendship.AddFriendResp;
import com.lld.im.seq.Seq;
import com.lld.im.service.ImFriendShipRequestService;
import com.lld.im.service.ImFriendShipService;
import com.lld.im.service.ImUserService;
import org.apache.commons.lang3.StringUtils;
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
    @Qualifier("snowflakeSeq")
    Seq seq;

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
                if(data.getFriendAllowType() == 1){
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
                        ResponseVO doAddFriend = imFriendShipService.doAddFriend(req.getAppId(),req.getFromId(),dto);
                        resp.setCode(doAddFriend.getCode());
                        resp.setMsg(doAddFriend.getMsg());
                        resp.setToId(toId);
                        result.add(resp);
                    }catch (Exception e){
                        resp.setCode(BaseErrorCode.SYSTEM_ERROR.getCode());
                        resp.setMsg(BaseErrorCode.SYSTEM_ERROR.getError());
                        resp.setToId(toId);
                        result.add(resp);
                    }
                }
            }
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
    public ResponseVO doAddFriend(Integer appId,String fromId,FriendDto dto){

        QueryWrapper queryFrom = new QueryWrapper<>()
                .eq("from_id", fromId)
                .eq("app_id",appId)
                .eq("to_id",dto.getToId());
        ImFriendShipEntity fromItem = imFriendShipMapper.selectOne(queryFrom);

        if(fromItem != null){
            if(fromItem.getStatus() == FriendShipEnum.FRIEND_STATUS_NORMAL.code){
                //重复添加
                return ResponseVO.errorResponse(FriendShipErrorCode.REPEAT_TO_ADD.getCode()
                        ,FriendShipErrorCode.REPEAT_TO_ADD.getError());
            } else{
                //将状态修改为正常，将设置备注，更新seq
                if(StringUtils.isNotEmpty(dto.getRemark())){
                    fromItem.setRemark(dto.getRemark());
                }
                if(StringUtils.isNotEmpty(dto.getAddWording())){
                    fromItem.setAddSource(dto.getAddSource());
                }
                fromItem.setSequence(seq.getSeq(appId+""));
                imFriendShipMapper.updateById(fromItem);
            }
        }else {
            fromItem = new ImFriendShipEntity();
            fromItem.setAddSource(dto.getAddSource());
            fromItem.setAppId(appId);
            fromItem.setBlack(FriendShipEnum.BLACK_STATUS_NORMAL.code);
            fromItem.setStatus(FriendShipEnum.FRIEND_STATUS_NORMAL.code);
            fromItem.setCreateTime(System.currentTimeMillis());
            fromItem.setRemark(dto.getRemark());
            fromItem.setToId(dto.getToId());
            fromItem.setFromId(fromId);
            fromItem.setSequence(seq.getSeq(appId+""));
            int insert = imFriendShipMapper.insert(fromItem);
            if(insert < 1){
                throw new ApplicationException(FriendShipErrorCode.FRIEND_ADD_ERROR);
            }
        }

        //判断to是否有添加from，如果没有，则插入数据
        QueryWrapper queryTo = new QueryWrapper<>()
                .eq("from_id", dto.getToId())
                .eq("app_id",appId)
                .eq("to_id",fromId);
        ImFriendShipEntity toItem = imFriendShipMapper.selectOne(queryTo);
        
        if(toItem == null){
            toItem = new ImFriendShipEntity();
            toItem.setAppId(appId);
            toItem.setBlack(FriendShipEnum.BLACK_STATUS_NORMAL.code);
            toItem.setStatus(FriendShipEnum.FRIEND_STATUS_NO_FRIEND.code);
            toItem.setCreateTime(System.currentTimeMillis());
            toItem.setToId(dto.getToId());
            toItem.setFromId(dto.getToId());
            toItem.setToId(fromId);
            toItem.setSequence(seq.getSeq(appId+""));
            int insert = imFriendShipMapper.insert(toItem);
            if(insert < 1){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                //添加失败
                return ResponseVO.errorResponse(FriendShipErrorCode.FRIEND_ADD_ERROR.getCode()
                        ,FriendShipErrorCode.FRIEND_ADD_ERROR.getError());
            }
        }

        return ResponseVO.successResponse();
    }

    @Override
    public ResponseVO deleteFriend(DeleteFriendReq req) {
        return null;
    }
}
