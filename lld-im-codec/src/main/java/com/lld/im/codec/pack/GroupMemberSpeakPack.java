package com.lld.im.codec.pack;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-10-13 16:23
 **/
@Data
public class GroupMemberSpeakPack {

    private String groupId;

    private String memberId;

    private Long speakDate;

}
