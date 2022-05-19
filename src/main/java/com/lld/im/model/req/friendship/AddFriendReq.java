package com.lld.im.model.req.friendship;

import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description: 添加好友
 * @create: 2022-05-19 09:17
 **/
@Data
public class AddFriendReq {

    private String fromId;

    private List<FriendDto> addFriendItems;

    private Integer appId;

}
