package com.lld.im.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.model.req.account.ImportUserReq;
import com.lld.im.model.req.friendship.AddFriendReq;
import com.lld.im.service.ImFriendShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 09:04
 **/
@RestController
@RequestMapping("friendship")
public class ImFriendShipController {

    @Autowired
    ImFriendShipService imFriendShipService;

    @RequestMapping("/addFriend")
    public ResponseVO addFriend(@RequestBody AddFriendReq req)  {//@Validated
        return imFriendShipService.addFriend(req);
    }

}
