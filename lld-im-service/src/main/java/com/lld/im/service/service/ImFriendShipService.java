package com.lld.im.service.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.model.req.friendship.AddFriendReq;
import com.lld.im.service.model.req.friendship.DeleteFriendReq;
import com.lld.im.service.model.req.friendship.FriendDto;

public interface ImFriendShipService {

    public ResponseVO addFriend(AddFriendReq req) ;

    public ResponseVO deleteFriend(DeleteFriendReq req);

    public ResponseVO doAddFriend(Integer appId,String fromId,FriendDto dto);
}
