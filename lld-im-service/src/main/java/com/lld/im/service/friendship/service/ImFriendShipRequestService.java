package com.lld.im.service.friendship.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.model.SyncReq;
import com.lld.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.lld.im.service.friendship.model.req.FriendDto;
import com.lld.im.service.friendship.model.req.ReadFriendShipRequestReq;

public interface ImFriendShipRequestService {

    public ResponseVO addFriendRequest(String fromId, Integer appId, FriendDto dto);

    public ResponseVO getFriendRequest(String fromId, Integer appId);

    public ResponseVO readAllFriendRequest(ReadFriendShipRequestReq req);

    public ResponseVO approverFriendRequest(ApproveFriendRequestReq req);

    public ResponseVO syncFriendShipRequest(SyncReq req);
}
