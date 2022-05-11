package com.lld.im.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.model.req.account.ImportUserReq;
import com.lld.im.service.UserService;
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
public class UserController {

    private static Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserService userService;

    @RequestMapping("/delRate")
//    @CheckToken
    public ResponseVO delRate(@RequestBody ImportUserReq req){//@Validated
        return userService.importUser(req);
    }

}
