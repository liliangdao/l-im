package com.lld.im.service.friendship.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.model.SyncReq;
import com.lld.im.service.friendship.model.req.*;
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
@RequestMapping("v1/friendship")
public class ImFriendShipController {

    @Autowired
    ImFriendShipService imFriendShipService;

    @RequestMapping("/importFriendShip")
    public ResponseVO importFriendShip(@RequestBody @Validated ImportFriendShipReq req,Integer appId)  {
        req.setAppId(appId);
        return imFriendShipService.importFriendShip(req);
    }

    @RequestMapping("/addFriend")
    public ResponseVO addFriend(@RequestBody @Validated AddFriendShipReq req,Integer appId)  {
        req.setAppId(appId);
        return imFriendShipService.addFriend(req);
    }

    @RequestMapping("/deleteFriend")
    public ResponseVO deleteFriend(@RequestBody @Validated DeleteFriendReq req,Integer appId)  {
        req.setAppId(appId);
        return imFriendShipService.deleteFriend(req);
    }

    @RequestMapping("/syncFriendShipList")
    public ResponseVO syncFriendShipList(@RequestBody @Validated SyncReq req, Integer appId)  {
        req.setAppId(appId);
        return imFriendShipService.syncFriendShipList(req);
    }

    @RequestMapping("/getAllFriendShip")
    public ResponseVO getAllFriendShip(@RequestBody @Validated GetAllFriendShipReq req,Integer appId)  {//@Validated
        req.setAppId(appId);
        return imFriendShipService.getAllFriendShip(req);
    }

    @RequestMapping("/updateFriendship")
    public ResponseVO updateFriendship(@RequestBody @Validated UpdateFriendshipReq req,Integer appId)  {//@Validated
        req.setAppId(appId);
        return imFriendShipService.updateFriendShip(req);
    }

    @RequestMapping("/getRelation")
    public ResponseVO getRelation(@RequestBody @Validated GetRelationReq req,Integer appId)  {//@Validated
        req.setAppId(appId);
        return imFriendShipService.getRelation(req);
    }

    @RequestMapping("/checkFriendShip")
    public ResponseVO checkFriendShip(@RequestBody @Validated CheckFriendShipReq req,Integer appId)  {//@Validated
        req.setAppId(appId);
        return imFriendShipService.checkFriend(req);
    }

    @RequestMapping("/addBlack")
    public ResponseVO addBlack(@RequestBody @Validated AddFriendShipBlackReq req,Integer appId)  {
        req.setAppId(appId);
        return imFriendShipService.addBlack(req);
    }

    @RequestMapping("/deleteBlack")
    public ResponseVO deleteBlack(@RequestBody @Validated DeleteBlackReq req,Integer appId)  {
        req.setAppId(appId);
        return imFriendShipService.deleteBlack(req);
    }

    @RequestMapping("/checkBlck")
    public ResponseVO addBlack(@RequestBody @Validated CheckFriendShipReq req,Integer appId)  {
        req.setAppId(appId);
        return imFriendShipService.checkBlck(req);
    }

}
