package com.lld.im.model.resp.friendship;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 09:32
 **/
@Data
public class AddFriendResp {

    private String toId;

    private Integer code;

    private String msg;

}
