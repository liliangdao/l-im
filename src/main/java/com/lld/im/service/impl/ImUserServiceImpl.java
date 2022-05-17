package com.lld.im.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lld.im.common.ResponseVO;
import com.lld.im.dao.ImUserDataEntity;
import com.lld.im.dao.mapper.ImUserDataMapper;
import com.lld.im.enums.UserErrorCode;
import com.lld.im.exception.ApplicationException;
import com.lld.im.model.req.account.DeleteUserReq;
import com.lld.im.model.req.account.GetUserInfoReq;
import com.lld.im.model.req.account.ImportUserReq;
import com.lld.im.model.resp.account.GetUserInfoResp;
import com.lld.im.model.resp.account.ImportUserResp;
import com.lld.im.service.ImUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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

        List<ImUserDataEntity> userDataEntities = imUserDataMapper.selectBatchIds(req.getUserIds());
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
    public ResponseVO getSingleUserInfo(String userId) {

        ImUserDataEntity ImUserDataEntity = imUserDataMapper.selectById(userId);
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
}
