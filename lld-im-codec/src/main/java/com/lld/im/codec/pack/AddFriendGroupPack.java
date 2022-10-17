package com.lld.im.codec.pack;

import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description: 用户创建好友分组通知包
 * @create: 2022-08-02 13:46
 **/
@Data
public class AddFriendGroupPack {
    public String fromId;

    private String groupName;

    /** 序列号*/
    private Long sequence;
}
