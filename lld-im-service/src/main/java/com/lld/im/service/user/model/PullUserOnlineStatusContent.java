package com.lld.im.service.user.model;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: lld
 * @createDate: 2022-09-23
 * @version: 1.0
 */
@Data
public class PullUserOnlineStatusContent {

    private Integer appId;

    private String userId;

    private List<String> userList;

}
