package com.lld.im.model.req.account;

import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-12 15:23
 **/
@Data
public class GetUserInfoReq {

    private List<String> userIds;

    private List<String> standardField;

    private List<String> customField;



}
