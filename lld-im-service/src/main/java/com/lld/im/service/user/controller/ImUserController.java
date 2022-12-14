package com.lld.im.service.user.controller;

import com.lld.im.common.BaseErrorCode;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.route.RouteHandle;
import com.lld.im.common.route.RouteInfo;
import com.lld.im.common.utils.RouteInfoParseUtil;
import com.lld.im.service.service.ImService;
import com.lld.im.service.user.model.req.DeleteUserReq;
import com.lld.im.service.user.model.req.ImportUserReq;
import com.lld.im.service.user.model.req.LoginReq;
import com.lld.im.service.user.model.req.PullUserOnlineStatusReq;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.user.service.UserStatusService;
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
@RequestMapping("v1/user")
public class ImUserController {

    private static Logger logger = LoggerFactory.getLogger(ImUserController.class);

    @Autowired
    ImUserService imUserService;

    @Autowired
    ImService imService;

    @Autowired
    RouteHandle routeHandle;

    @Autowired
    UserStatusService userStatusService;

    @RequestMapping("/importUser")
    public ResponseVO importUser(@RequestBody ImportUserReq req,Integer appId){//@Validated
        req.setAppId(appId);
        return imUserService.importUser(req);
    }

    @RequestMapping("/deleteUser")
    public ResponseVO deleteUser(@RequestBody DeleteUserReq req,Integer appId){//@Validated
        req.setAppId(appId);
        return imUserService.deleteUser(req);
    }

    /**
     * @description im的登录接口
     * @author chackylee
     * @date 2022/5/17 10:23
     * @param [req]
     * @return com.lld.im.common.ResponseVO
    */
    @RequestMapping("/login")
    public ResponseVO login(@RequestBody @Validated LoginReq req){//@Validated
        ResponseVO login = imUserService.login(req.getUserId());
        if(login.getCode() == BaseErrorCode.SUCCESS.getCode()){
            //返回im服务地址
            String serverUrl = routeHandle.routeServer(imService.getAllImServerList(req.getClientType()), req.getUserId());
            RouteInfo route = RouteInfoParseUtil.parse(serverUrl);
            return ResponseVO.successResponse(route);
        }
        return login;
    }

    /**
     * @description 查询用户在线状态
     * @author chackylee
     * @date 2022/9/30 15:58
     * @param [req]
     * @return com.lld.im.common.ResponseVO
    */
    @RequestMapping("/queryOnlineStatus")
    public ResponseVO queryOnlineStatus(@RequestBody @Validated PullUserOnlineStatusReq req){
        return ResponseVO.successResponse(userStatusService.pullAllUserOnlineStatus(req));
    }

}
