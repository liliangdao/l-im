package com.lld.im.codec.pack.group;

import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description: 群内添加群成员通知报文
 * @create: 2022-09-30 15:16
 **/
@Data
public class AddGroupMemberPack {

    private String groupId;

    private List<String> members;

}
