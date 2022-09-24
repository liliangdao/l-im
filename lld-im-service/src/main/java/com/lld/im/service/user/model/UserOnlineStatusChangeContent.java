package com.lld.im.service.user.model;

import com.lld.im.common.model.ClientInfo;
import lombok.Data;
import sun.dc.pr.PRError;

/**
 * @description:
 * @author: lld
 * @createDate: 2022-09-23
 * @version: 1.0
 */
@Data
public class UserOnlineStatusChangeContent {

    private Integer appId;

    private String userId;

    private String status;

    //客户端状态由业务传递
    private int customStatus;
    //客户端状态字符串
    private String customText;

}
