package com.lld.im.controller;

import com.lld.im.common.BaseErrorCode;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.route.RouteHandle;
import com.lld.im.common.route.RouteInfo;
import com.lld.im.model.req.account.GetUserInfoReq;
import com.lld.im.model.req.account.ImportUserReq;
import com.lld.im.model.req.account.LoginReq;
import com.lld.im.model.req.account.UserId;
import com.lld.im.service.ImService;
import com.lld.im.service.ImUserService;
import com.lld.im.utils.RouteInfoParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-11 14:22
 **/
@RestController
@RequestMapping("user")
public class ImUserController {

    private static Logger logger = LoggerFactory.getLogger(ImUserController.class);

    @Autowired
    ImUserService imUserService;

    @Autowired
    ImService imService;

    @Autowired
    RouteHandle routeHandle;

    @RequestMapping("/importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req){//@Validated
        return imUserService.importUser(req);
    }

    /**
     * @description im的登录接口
     * @author chackylee
     * @date 2022/5/17 10:23
     * @param [req]
     * @return com.lld.im.common.ResponseVO
    */
    @RequestMapping("/login")
    public ResponseVO login(@RequestBody LoginReq req){//@Validated
        ResponseVO login = imUserService.login(req.getUserId());
        if(login.getCode() == BaseErrorCode.SUCCESS.getCode()){
            //返回im服务地址
            String serverUrl = routeHandle.routeServer(imService.getAllImServerList(req.getClientType()), req.getUserId());
            RouteInfo route = RouteInfoParseUtil.parse(serverUrl);
            return ResponseVO.successResponse(route);
        }
        return login;
    }

}
