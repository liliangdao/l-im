package com.lld.im.codec.pack.friendship;

import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description: 删除好友分组成员通知报文
 * @create: 2022-08-02 13:46
 **/
@Data
public class DeleteFriendGroupMemberPack {

    public String fromId;

    private String groupName;

    private List<String> toIds;

    /** 序列号*/
    private Long sequence;
}
