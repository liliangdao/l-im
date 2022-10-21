package com.lld.im.codec.pack.user;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 服务端发给客户端、登录ack返回报文
 * @create: 2022-05-05 09:26
 **/
@Data
public class LoginAckPack {

    private String userId;


}
