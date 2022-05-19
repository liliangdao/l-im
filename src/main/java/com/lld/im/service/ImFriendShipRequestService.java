package com.lld.im.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.model.req.friendship.FriendDto;

public interface ImFriendShipRequestService {

    public ResponseVO addFriendRequest(String fromId, Integer appId, FriendDto dto);

}
