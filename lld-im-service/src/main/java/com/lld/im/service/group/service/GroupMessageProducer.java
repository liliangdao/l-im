package com.lld.im.service.group.service;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.enums.command.GroupEventCommand;
import com.lld.im.service.group.model.req.GroupMemberDto;
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

    public <T> void producer(Integer command,Object data) {

        JSONObject o = (JSONObject) JSONObject.toJSON(data);
        int appId = o.getIntValue("appId");
        String groupId = o.getString("groupId");

        if(command.equals(GroupEventCommand.JOIN_GROUP.getCommand())){
            //申请入群 推送给管理员跟自己
            List<GroupMemberDto> groupManager = groupMemberService.getGroupManager(groupId, appId);
        }


    }

}
