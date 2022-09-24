package com.lld.im.service.user.model;

import lombok.Data;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/9/24
 * @version: 1.0
 */
@Data
public class UserOnlineStatusSubscribeContent {

    private Integer appId;

    private String userId;

    //被订阅的id
    private String beSubUserId;

    //订阅时间 单位毫秒 0表示持久订阅
    private Long subTime;

//    {
//        "appId":"",
//        "userId":"",
//        "beSubUserId":"",
//        "subTime":""
//    }

}
