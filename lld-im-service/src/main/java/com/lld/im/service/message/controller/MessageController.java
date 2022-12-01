package com.lld.im.service.message.controller;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.model.SyncReq;
import com.lld.im.common.model.msg.CheckSendMessageReq;
import com.lld.im.service.group.model.req.CreateGroupReq;
import com.lld.im.service.message.model.req.SendMessageReq;
import com.lld.im.service.message.service.MessageSyncService;
import com.lld.im.service.message.service.P2PMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-22 15:10
 **/
@RestController
@RequestMapping("v1/message")
public class MessageController {

    @Autowired
    MessageSyncService messageSyncService;

    @Autowired
    P2PMessageService p2PMessageService;

    @RequestMapping("/syncOfflineMessage")
    public ResponseVO syncOfflineMessage(@RequestBody @Validated SyncReq req, Integer appId)  {
        req.setAppId(appId);
        return messageSyncService.syncOfflineMessage(req
        );
    }

    @RequestMapping("/send")
    public ResponseVO send(@RequestBody @Validated SendMessageReq req, Integer appId)  {
        req.setAppId(appId);
        return ResponseVO.successResponse(p2PMessageService.send(req));
    }

    @RequestMapping("/checkSendP2P")
    public ResponseVO checkSendP2P(@RequestBody CheckSendMessageReq req)  {
        return ResponseVO.successResponse(p2PMessageService.imServerpermissionCheck(
                req.getFromId(),req.getToId(),req.getAppId()
        ));
    }


}
