package com.lld.im.codec.pack;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-09-09 10:19
 **/
@Data
public class ApproverFriendRequestPack extends BasePack {

    private Long id;

    //1同意 2拒绝
    private Integer status;
}
