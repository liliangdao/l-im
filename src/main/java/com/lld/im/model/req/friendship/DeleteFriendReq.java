package com.lld.im.model.req.friendship;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 09:18
 **/
@Data
public class DeleteFriendReq {

    private String fromId;

    private String toId;

    private Integer appId;
}
