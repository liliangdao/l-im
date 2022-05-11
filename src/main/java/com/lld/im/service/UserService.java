package com.lld.im.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.model.req.account.ImportUserReq;

public interface UserService {

    public ResponseVO importUser(ImportUserReq req);

}
