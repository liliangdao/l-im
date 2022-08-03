package com.lld.im.service.friendship.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.friendship.model.req.ApproveFriendRequestReq;
import com.lld.im.service.friendship.model.req.FriendDto;

public interface ImFriendShipRequestService {

    public ResponseVO addFriendRequest(String fromId, Integer appId, FriendDto dto);

    public ResponseVO getFriendRequest(String fromId, Integer appId);

    public ResponseVO readAllFriendRequest(String fromId, Integer appId);

    public ResponseVO approverFriendRequest(ApproveFriendRequestReq req);
}
