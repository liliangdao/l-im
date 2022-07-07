package com.lld.im.service.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.model.req.friendship.FriendDto;

public interface ImFriendShipRequestService {

    public ResponseVO addFriendRequest(String fromId, Integer appId, FriendDto dto);

}
