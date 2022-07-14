package com.lld.im.service.group.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.friendship.model.req.AddFriendReq;
import com.lld.im.service.friendship.service.ImFriendShipService;
import com.lld.im.service.group.model.req.CreateGroupReq;
import com.lld.im.service.group.model.req.DestroyGroupReq;
import com.lld.im.service.group.model.req.GetJoinedGroupReq;
import com.lld.im.service.group.model.req.UpdateGroupReq;
import com.lld.im.service.group.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/9
 * @version: 1.0
 */
@RestController
@RequestMapping("v1/group")
public class GroupController {


    @Autowired
    GroupService groupService;

    @RequestMapping("/createGroup")
    public ResponseVO createGroup(@RequestBody @Validated CreateGroupReq req,Integer appId,String identifier)  {
        req.setAppId(appId);
        req.setOperater(identifier);
        return groupService.createGroup(req);
    }

    @RequestMapping("/getGroupInfo")
    public ResponseVO getGroupInfo(@RequestBody @Validated CreateGroupReq req,Integer appId)  {
        req.setAppId(appId);
        return groupService.createGroup(req);
    }

    @RequestMapping("/update")
    public ResponseVO update(@RequestBody @Validated UpdateGroupReq req, Integer appId,String identifier)  {
        req.setAppId(appId);
        req.setOperater(identifier);
        return groupService.updateBaseGroupInfo(req);
    }

    @RequestMapping("/getJoinedGroup")
    public ResponseVO getJoinedGroup(@RequestBody @Validated GetJoinedGroupReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperater(identifier);
        return groupService.getJoinedGroup(req);
    }

    @RequestMapping("/destroyGroup")
    public ResponseVO destroyGroup(@RequestBody @Validated DestroyGroupReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperater(identifier);
        return groupService.destroyGroup(req);
    }

}
