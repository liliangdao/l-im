package com.lld.im.codec.pack;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 用户添加黑名单以后tcp通知数据包
 * @create: 2022-08-02 13:46
 **/
@Data
public class AddFriendBlackPack {
    private String fromId;

    private String toId;

    private Long sequence;
}
