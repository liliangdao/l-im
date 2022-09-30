package com.lld.im.common.model;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-14 11:28
 **/
@Data
public class RequestBase {
    private Integer appId;

    private Integer clientType;

    private String operater;

    private String imel;
}
