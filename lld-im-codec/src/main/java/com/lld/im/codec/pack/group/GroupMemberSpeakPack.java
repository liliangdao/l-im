package com.lld.im.codec.pack.group;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 群成员禁言通知报文
 * @create: 2022-10-13 16:23
 **/
@Data
public class GroupMemberSpeakPack {

    private String groupId;

    private String memberId;

    private Long speakDate;

}
