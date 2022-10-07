package com.lld.im.service.group.service;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.pack.AddGroupMemberPack;
import com.lld.im.codec.pack.RemoveGroupMemberPack;
import com.lld.im.common.ClientType;
import com.lld.im.common.enums.command.Command;
import com.lld.im.common.enums.command.GroupEventCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.service.group.model.req.GroupMemberDto;
import com.lld.im.service.message.service.MessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveStreamCommands;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @author: lld
 * @createDate: 2022-09-21
 * @version: 1.0
 */
@Service
public class GroupMessageProducer {

    @Autowired
    GroupMemberService groupMemberService;

    @Autowired
    MessageProducer messageProducer;

    public <T> void producer(String userId, Command command, Object data, ClientInfo clientInfo) {

        JSONObject o = (JSONObject) JSONObject.toJSON(data);
        int appId = o.getIntValue("appId");
        String groupId = o.getString("groupId");

        if(command.equals(GroupEventCommand.ADDED_MEMBER.getCommand())){
            //申请入群 推送给管理员跟自己
            List<GroupMemberDto> groupManager = groupMemberService.getGroupManager(groupId, appId);
            AddGroupMemberPack addGroupMemberPack = o.toJavaObject(AddGroupMemberPack.class);
            List<String> members = addGroupMemberPack.getMembers();
            groupManager.forEach(e->{
                if(clientInfo.getClientType() != ClientType.WEBAPI.getCode() && !e.getMemberId().equals(userId)){
                    messageProducer.sendToUserExceptClient(e.getMemberId(),command,data,clientInfo);
                }else{
                    messageProducer.sendToUser(e.getMemberId(),command,data,clientInfo.getAppId());
                }
            });
            members.forEach(e ->{
                if(clientInfo.getClientType() != ClientType.WEBAPI.getCode() && !e.equals(userId)){
                    messageProducer.sendToUserExceptClient(e,command,data,clientInfo);
                }else{
                    messageProducer.sendToUser(e,command,data,clientInfo.getAppId());
                }
            });
        }else if(command.equals(GroupEventCommand.DELETED_MEMBER.getCommand())){
            RemoveGroupMemberPack pack = o.toJavaObject(RemoveGroupMemberPack.class);
            List<String> groupMemberId = groupMemberService.getGroupMemberId(pack.getGroupId(), clientInfo.getAppId());
            groupMemberId.add(pack.getMember());
            groupMemberId.forEach(e->{
                if(clientInfo.getClientType() != ClientType.WEBAPI.getCode() && !e.equals(userId)){
                    messageProducer.sendToUserExceptClient(e,command,data,clientInfo);
                }else{
                    messageProducer.sendToUser(e,command,data,clientInfo.getAppId());
                }
            });

        }else{
            List<String> groupMemberId = groupMemberService.getGroupMemberId(groupId, clientInfo.getAppId());
            groupMemberId.forEach(e->{
                if(clientInfo.getClientType() != null && clientInfo.getClientType() != ClientType.WEBAPI.getCode() && !e.equals(userId)){
                    messageProducer.sendToUserExceptClient(e,command,data,clientInfo);
                }else{
                    messageProducer.sendToUser(e,command,data,clientInfo.getAppId());
                }
            });
        }
    }



}
