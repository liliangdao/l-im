package com.lld.im.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.model.req.account.DeleteUserReq;
import com.lld.im.model.req.account.GetUserInfoReq;
import com.lld.im.model.req.account.ImportUserReq;

public interface ImUserService {

    public ResponseVO importUser(ImportUserReq req);

    /**
     * @description 获取用户详情
     * @author chackylee
     * @date 2022/5/12 15:25
     * @param [req]
     * @return com.lld.im.common.ResponseVO
     */
    public ResponseVO getUserInfo(GetUserInfoReq req);

    public ResponseVO getSingleUserInfo(String userId);

    public ResponseVO login(String userId);

    public ResponseVO deleteUser(DeleteUserReq req);
}
