package com.lld.im.service.friendship.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.model.RequestBase;
import com.lld.im.common.model.SyncReq;
import com.lld.im.service.friendship.dao.ImFriendShipEntity;
import com.lld.im.service.friendship.model.req.*;
import com.lld.im.service.friendship.model.resp.UpdateFriendshipResp;

import java.util.List;

public interface ImFriendShipService {

    public ResponseVO addFriend(AddFriendShipReq req) ;

    public ResponseVO deleteFriend(DeleteFriendReq req);

    public ResponseVO doAddFriend(RequestBase requestBase, String fromId, FriendDto dto);

    public ResponseVO syncFriendShipList(SyncReq req);

    public ResponseVO getAllFriendShip(GetAllFriendShipReq req);

    public ResponseVO<ImFriendShipEntity> getRelation(GetRelationReq req);

    public ResponseVO<List<UpdateFriendshipResp>> updateFriendShip(UpdateFriendshipReq req);

    public ResponseVO checkFriend(CheckFriendShipReq req);

    public ResponseVO addBlack(AddFriendShipBlackReq req);

    public ResponseVO deleteBlack(DeleteBlackReq req);

    ResponseVO checkBlck(CheckFriendShipReq req);
}
