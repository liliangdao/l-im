package com.lld.im.service.impl;

import com.lld.im.common.ResponseVO;
import com.lld.im.controller.UserController;
import com.lld.im.dao.UserDataEntity;
import com.lld.im.dao.mapper.UserDataMapper;
import com.lld.im.enums.UserErrorCode;
import com.lld.im.exception.ApplicationException;
import com.lld.im.model.req.account.ImportUserReq;
import com.lld.im.model.resp.account.ImportUserResp;
import com.lld.im.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-11 14:30
 **/
@Service
public class UserServiceImpl implements UserService {

    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserDataMapper userDataMapper;

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

        LinkedList<String> errorId = new LinkedList();
        LinkedList<String> successId = new LinkedList();


        for (UserDataEntity data : req.getUserData()) {
            data.setAppId(req.getAppId());
            try {
                userDataMapper.insert(data);
                successId.add(data.getUserId());
            }catch (Exception e){
                logger.error("导入用户失败 ： appId:{} ,userId : {}",data.getAppId(),data.getUserId());
                errorId.add(data.getUserId());
            }
        }

        ImportUserResp resp = new ImportUserResp();
        resp.setErrorId(errorId);
        resp.setSuccessId(successId);

        return ResponseVO.successResponse(resp);
    }
}
