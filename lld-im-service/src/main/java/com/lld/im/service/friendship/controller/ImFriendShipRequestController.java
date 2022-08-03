package com.lld.im.service.friendship.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.friendship.model.req.*;
import com.lld.im.service.friendship.service.ImFriendShipRequestService;
import com.lld.im.service.friendship.service.ImFriendShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 09:04
 **/
@RestController
@RequestMapping("v1/friendshipRequest")
public class ImFriendShipRequestController {

    @Autowired
    ImFriendShipRequestService imFriendShipRequestService;

    @RequestMapping("/getFriendRequest")
    public ResponseVO getFriendRequest(@RequestBody @Validated GetFriendShipRequestReq req,Integer appId)  {//@Validated
        return imFriendShipRequestService.getFriendRequest(req.getFromId(),appId);
    }

    @RequestMapping("/readAllFriendRequest")
    public ResponseVO readAllFriendRequest(@RequestBody @Validated ReadFriendShipRequestReq req,Integer appId)  {//@Validated
        return imFriendShipRequestService.readAllFriendRequest(req.getFromId(),appId);
    }

    @RequestMapping("/approverFriendRequest")
    public ResponseVO approverFriendRequest(@RequestBody @Validated ApproveFriendRequestReq req,Integer appId)  {//@Validated
        return imFriendShipRequestService.approverFriendRequest(req);
    }



}
