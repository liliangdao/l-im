package com.lld.im.codec.pack.friendship;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 已读好友申请通知报文
 * @create: 2022-09-09 10:15
 **/
@Data
public class ReadAllFriendRequestPack {

    private String fromId;

    private Long sequence;
}
