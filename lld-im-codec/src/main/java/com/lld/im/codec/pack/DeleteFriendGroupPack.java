package com.lld.im.codec.pack;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-08-02 13:46
 **/
@Data
public class DeleteFriendGroupPack {
    public String fromId;

    private String groupName;

    /** 序列号*/
    private Long sequence;
}
