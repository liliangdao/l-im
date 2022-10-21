package com.lld.im.codec.pack.user;

import com.lld.im.common.model.UserSession;
import lombok.Data;
import sun.dc.pr.PRError;

import java.util.List;

/**
 * @description: 服务端发给客户端，status区分是上线还是下线
 * @author: lld
 * @createDate: 2022/9/25
 * @version: 1.0
 */
@Data
public class UserStatusChangeNotifyPack {

    private Integer appId;

    private String userId;

    //服务端状态 1上线 2离线
    private Integer status;

    //客户端状态由业务传递 登录时可能会传递
    private Integer customStatus;
    //客户端状态字符串 登录时可能会传递
    private String customText;

    private List<UserSession> client;

}
