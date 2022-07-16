package com.lld.im.service.friendship.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.friendship.model.req.*;

public interface ImFriendShipService {

    public ResponseVO addFriend(AddFriendReq req) ;

    public ResponseVO deleteFriend(DeleteFriendReq req);

    public ResponseVO doAddFriend(Integer appId,String fromId,FriendDto dto);

    public ResponseVO syncFriendShipList(SyncFriendShipReq req);

    public ResponseVO getAllFriendShip(GetAllFriendShipReq req);

    public ResponseVO getRelation(GetRelationReq req);
}
