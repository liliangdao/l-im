package com.lld.im.codec.pack;

import com.lld.im.common.model.UserSession;
import lombok.Data;
import sun.dc.pr.PRError;

import java.util.List;

/**
 * @description: 用户状态变更通知报文
 * @author: lld
 * @createDate: 2022/9/25
 * @version: 1.0
 */
@Data
public class UserStatusChangeNotifyPack {

    private Integer appId;

    private String userId;

    //服务端状态
    private Integer status;

    private Integer clientType;

    //客户端状态由业务传递
    private Integer customStatus;
    //客户端状态字符串
    private String customText;

    private List<UserSession> client;

}
