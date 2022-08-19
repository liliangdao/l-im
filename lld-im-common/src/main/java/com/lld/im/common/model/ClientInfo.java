package com.lld.im.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-22 15:52
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientInfo {

    private Integer appId;

    private int clientType;

    private String imei;


}
