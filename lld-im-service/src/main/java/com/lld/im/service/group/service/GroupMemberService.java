package com.lld.im.service.group.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.model.SyncReq;
import com.lld.im.service.group.model.req.*;
import com.lld.im.service.group.model.resp.GetRoleInGroupResp;

import java.util.Collection;
import java.util.List;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/10
 * @version: 1.0
 */
public interface GroupMemberService {

    public ResponseVO addMember(AddGroupMemberReq req);

    public ResponseVO removeMember(RemoveGroupMemberReq req);

    public ResponseVO addGroupMember(String groupId, Integer appId, GroupMemberDto dto);

    public ResponseVO removeGroupMember(String groupId, Integer appId, String memberId);

    public ResponseVO<GetRoleInGroupResp> getRoleInGroupOne(String groupId, String memberId, Integer appId);

    public ResponseVO<List<GetRoleInGroupResp>> getRoleInGroup(GetRoleInGroupReq req);

    public ResponseVO<Collection<String>> getMemberJoinedGroup(GetJoinedGroupReq req);

    public ResponseVO<Collection<String>> syncMemberJoinedGroup(SyncReq req);

    public ResponseVO<List<GroupMemberDto>> getGroupMember(String groupId,Integer appId);

    public List<GroupMemberDto> getGroupManager(String groupId,Integer appId);

}
