package com.lld.im.service.friendship.model.resp;

import lombok.Data;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/9
 * @version: 1.0
 */
@Data
public class GetFriendRequestResp {

    private int id;

    private String fromId;

    private String toId;

    private Integer appId;

    private Integer readStatus;

    //打招呼信息
    private String addWording;
    //添加来源
    private String addSource;

    private String nickName;

    //性别 1男 2女 0未设置/未知
    private Integer userSex;

    private String photo;



}
