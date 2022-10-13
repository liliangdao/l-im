package com.lld.im.service.group.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.model.SyncReq;
import com.lld.im.service.group.model.req.*;
import com.lld.im.service.group.service.GroupMessageService;
import com.lld.im.service.group.service.ImGroupMemberService;
import com.lld.im.service.group.service.ImGroupService;
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
@RequestMapping("v1/group/member")
public class ImGroupMemberController {

    @Autowired
    ImGroupMemberService groupMemberService;

    @RequestMapping("/importGroupMember")
    public ResponseVO importGroupMember(@RequestBody @Validated ImportGroupMemberReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperater(identifier);
        return groupMemberService.importGroupMember(req);
    }

    @RequestMapping("/add")
    public ResponseVO addMember(@RequestBody @Validated AddGroupMemberReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperater(identifier);
        return groupMemberService.addMember(req);
    }

    @RequestMapping("/remove")
    public ResponseVO removeMember(@RequestBody @Validated RemoveGroupMemberReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperater(identifier);
        return groupMemberService.removeMember(req);
    }

    @RequestMapping("/update")
    public ResponseVO updateGroupMember(@RequestBody @Validated UpdateGroupMemberReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperater(identifier);
        return groupMemberService.updateGroupMember(req);
    }

    @RequestMapping("/speak")
    public ResponseVO speak(@RequestBody @Validated SpeaMemberReq req, Integer appId, String identifier)  {
        req.setAppId(appId);
        req.setOperater(identifier);
        return groupMemberService.speak(req);
    }

}
