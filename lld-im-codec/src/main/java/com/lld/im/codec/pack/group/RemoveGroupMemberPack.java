package com.lld.im.codec.pack.group;

import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description: 踢人出群通知报文
 * @create: 2022-09-30 15:16
 **/
@Data
public class RemoveGroupMemberPack {

    private String groupId;

    private String member;

}
