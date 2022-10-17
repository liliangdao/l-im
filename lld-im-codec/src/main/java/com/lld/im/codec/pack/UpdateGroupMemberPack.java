package com.lld.im.codec.pack;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 修改群成员通知报文
 * @create: 2022-09-30 15:00
 **/
@Data
public class UpdateGroupMemberPack {

    private String groupId;

    private String memberId;

    private String alias;

    private String extra;
}
