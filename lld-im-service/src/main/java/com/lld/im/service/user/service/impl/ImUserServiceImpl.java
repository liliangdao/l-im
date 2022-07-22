package com.lld.im.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.DelFlagEnum;
import com.lld.im.service.Application;
import com.lld.im.service.service.seq.Seq;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.dao.mapper.ImUserDataMapper;
import com.lld.im.common.enums.UserErrorCode;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.service.user.model.req.DeleteUserReq;
import com.lld.im.service.user.model.req.GetUserInfoReq;
import com.lld.im.service.user.model.req.ImportUserReq;
import com.lld.im.service.user.model.req.ModifyUserInfoReq;
import com.lld.im.service.user.model.resp.GetUserInfoResp;
import com.lld.im.service.user.model.resp.ImportUserResp;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.utils.WriteUserSeq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @create: 2022-05-11 14:30
 **/
@Service
public class ImUserServiceImpl implements ImUserService {

    private static Logger logger = LoggerFactory.getLogger(ImUserServiceImpl.class);

    @Autowired
    ImUserDataMapper imUserDataMapper;

    @Autowired
    WriteUserSeq writeUserSeq;

    @Autowired
    @Qualifier("snowflakeSeq")
    Seq seq;

    /**
     * @description 導入用戶
     * @author chackylee
     * @date 2022/5/11 13:52
     * @param [req]
     * @return com.lld.im.common.ResponseVO
    */
    @Override
    @Transactional
    public ResponseVO importUser(ImportUserReq req) {

        if(req.getUserData().size() > 100){
            throw new ApplicationException(UserErrorCode.IMPORT_SIZE_BEYOND);
        }

        List<String> errorId = new ArrayList();
        List<String> successId = new ArrayList();


        for (ImUserDataEntity data : req.getUserData()) {
            data.setAppId(req.getAppId());
            try {
                imUserDataMapper.insert(data);
                successId.add(data.getUserId());
            }catch (Exception e){
                e.printStackTrace();
                logger.error("导入用户失败 ： appId:{} ,userId : {}",data.getAppId(),data.getUserId());
                errorId.add(data.getUserId());
            }
        }

        ImportUserResp resp = new ImportUserResp();
        resp.setErrorId(errorId);
        resp.setSuccessId(successId);

        return ResponseVO.successResponse(resp);
    }

    /**
     * @description 获取用户详情,TODO先简单做根据id查询，后面的过滤查询先不做
     * @author chackylee
     * @date 2022/5/12 15:26
     * @param [req]
     * @return com.lld.im.common.ResponseVO
    */
    @Override
    public ResponseVO getUserInfo(GetUserInfoReq req) {

        QueryWrapper<ImUserDataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app_id",req.getAppId());
        queryWrapper.in("user_id",req.getUserIds());

        List<ImUserDataEntity> userDataEntities = imUserDataMapper.selectList(queryWrapper);
        HashMap<String, ImUserDataEntity> map = new HashMap<>();

        for (ImUserDataEntity data:
        userDataEntities) {
            map.put(data.getUserId(),data);
        }

        List<String> failUser = new ArrayList<>();
        for (String uid:
             req.getUserIds()) {
            if(!map.containsKey(uid)){
                failUser.add(uid);
            }
        }

        GetUserInfoResp resp = new GetUserInfoResp();
        resp.setUserDataItem(userDataEntities);
        resp.setFailUser(failUser);
        return ResponseVO.successResponse(resp);
    }

    /**
     * @description 获取单个用户详情
     * @author chackylee
     * @date 2022/5/12 15:25
     * @param [req]
     * @return com.lld.im.common.ResponseVO
     */
    @Override
    public ResponseVO<ImUserDataEntity> getSingleUserInfo(String userId,Integer appId) {

        QueryWrapper objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("app_id",appId);
        objectQueryWrapper.eq("user_id",userId);
        objectQueryWrapper.eq("del_flag", DelFlagEnum.NORMAL.getCode());

        ImUserDataEntity ImUserDataEntity = imUserDataMapper.selectOne(objectQueryWrapper);
        if(ImUserDataEntity == null){
            throw new ApplicationException(UserErrorCode.USER_IS_NOT_EXIST);
        }

        return ResponseVO.successResponse(ImUserDataEntity);
    }


    /**
     * @description 登录用户，目前不实现
     * @author chackylee
     * @date 2022/5/17 17:04
     * @param [userId]
     * @return com.lld.im.common.ResponseVO
    */
    @Override
    public ResponseVO login(String userId) {
        return ResponseVO.successResponse();
    }

    /**
     * @description 删除用户
     * @author chackylee
     * @date 2022/5/17 17:04
     * @param [req]
     * @return com.lld.im.common.ResponseVO
    */
    @Override
    public ResponseVO deleteUser(DeleteUserReq req) {

        ImUserDataEntity entity = new ImUserDataEntity();
        entity.setDelFlag(1);

        List<String> errorId = new ArrayList();
        List<String> successId = new ArrayList();

        for (String userId:
        req.getUserId()) {
            QueryWrapper wrapper = new QueryWrapper();
            wrapper.eq("app_id",req.getAppId());
            wrapper.eq("user_id",userId);
            int update = 0;

            try {
                update =  imUserDataMapper.update(entity, wrapper);
                if(update > 0){
                    successId.add(userId);
                }else{
                    errorId.add(userId);
                }
            }catch (Exception e){
                errorId.add(userId);
            }
        }

        ImportUserResp resp = new ImportUserResp();
        resp.setSuccessId(successId);
        resp.setErrorId(errorId);
        return ResponseVO.successResponse(resp);
    }

    @Override
    public ResponseVO modifyUserInfo(ModifyUserInfoReq req) {


        QueryWrapper query = new QueryWrapper<>();
        query.eq("app_id",req.getAppId());
        query.eq("user_id",req.getUserId());
        ImUserDataEntity user = imUserDataMapper.selectOne(query);
        if(user == null){
            throw new ApplicationException(UserErrorCode.USER_IS_NOT_EXIST);
        }

        ImUserDataEntity update = new ImUserDataEntity();
        BeanUtils.copyProperties(req,update);

        update.setAppId(null);
        update.setUserId(null);
        long seq = this.seq.getSeq(req.getAppId() + Constants.SeqConstants.User);
        update.setSequence(seq);
        imUserDataMapper.update(update,query);
        writeUserSeq.writeUserSeq(req.getAppId(),req.getUserId(),Constants.SeqConstants.User,seq);
        //TODO 发送Tcp通知给用户

        return ResponseVO.successResponse();
    }
}
