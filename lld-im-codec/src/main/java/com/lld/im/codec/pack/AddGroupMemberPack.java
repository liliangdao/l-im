package com.lld.im.codec.pack;

import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-09-30 15:16
 **/
@Data
public class AddGroupMemberPack {

    private String groupId;

    private List<String> members;

}
