package com.lld.im.service.friendship.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.friendship.model.req.AddFriendShipGroupMemberReq;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-08-25 11:24
 **/
public interface ImFriendShipGroupMemberService {

    public ResponseVO addGroupMember(AddFriendShipGroupMemberReq req);

    public int doAddGroupMember(Long groupId,String toId);

    public int clearGroupMember(Long groupId);
}
