package com.lld.im.service.user.model.req;

import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.RequestBase;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/9/24
 * @version: 1.0
 */
@Data
public class SubscribeUserOnlineStatusReq extends RequestBase {

    //被订阅的id
    private Set<String> subUserId;

    //订阅时间 单位毫秒 0表示持久订阅
    private Long subTime;

//    {
//        "appId":"",
//        "userId":"",
//        "beSubUserId":"",
//        "subTime":""
//    }

}
