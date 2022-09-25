package com.lld.im.common.model;

import com.lld.im.common.enums.UserPipelineConnectState;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author: Chackylee
 * @description: 用户session信息
 * @create: 2022-05-05 09:20
 **/
@Data
@NoArgsConstructor
public class UserSession {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 应用ID
     */
    private Integer appId;

    /**
     * 端的标识
     */
    private Integer clientType;

    /**
     * 管道IP地址
     */
    private String pipelineHost;

    private String imei;

    /**
     * 所用的SDK版本
     */
    private String version;

    /**
     * 管道链接状态,1=在线，2=离线。
     *
     */
    private Integer connectState;

    private String mqRouteKey;

    private Long lastTime;

}
