package com.lld.im.service.group.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.model.SyncReq;
import com.lld.im.service.group.model.req.*;
import com.lld.im.service.group.service.GroupMemberService;
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

    @Autowired
    GroupMemberService groupMemberService;

    @RequestMapping("/importGroup")
    public ResponseVO importGroup(@RequestBody @Validated ImportGroupReq req,Integer appId,String identifier)  {
        req.setAppId(appId);
        req.setOperater(identifier);
        return groupService.importGroup(req);
    }

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

    @RequestMapping("/syncJoinedGroup")
    public ResponseVO syncJoinedGroup(@RequestBody @Validated SyncReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        return groupService.syncJoinedGroupList(req);
    }


    @RequestMapping("/destroyGroup")
    public ResponseVO destroyGroup(@RequestBody @Validated DestroyGroupReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperater(identifier);
        return groupService.destroyGroup(req);
    }

    @RequestMapping("/importGroupMember")
    public ResponseVO importGroupMember(@RequestBody @Validated ImportGroupMemberReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperater(identifier);
        return groupMemberService.importGroupMember(req);
    }

    @RequestMapping("/addMember")
    public ResponseVO addMember(@RequestBody @Validated AddGroupMemberReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperater(identifier);
        return groupMemberService.addMember(req);
    }

    @RequestMapping("/removeMember")
    public ResponseVO removeMember(@RequestBody @Validated RemoveGroupMemberReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperater(identifier);
        return groupMemberService.removeMember(req);
    }

    @RequestMapping("/get")
    public ResponseVO get(@RequestBody @Validated GetGroupReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperater(identifier);
        return groupService.getGroup(req);
    }

}
