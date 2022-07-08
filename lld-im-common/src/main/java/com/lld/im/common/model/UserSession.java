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

//    private Integer pipelineRpcPort;

//    public UserSession(Integer appId,Integer clientType) {
//        Integer port = null;
////        AppConfig appConfig = SpringBeanFactory.getBean(AppConfig.class);
////        if(req.getClientType() == 1){
////            port = appConfig.getWebSocketPort();//webSocketPort
////        }else{
////            port = appConfig.getTcpPort();
////        }
//
//        this.setAppId(req.getAppId());
//        this.setClientType(req.getClientType());
//        this.setConnectState(UserPipelineConnectState.ONLINE.getCommand());
//        this.setUserId(req.getUserId());
//        this.setImei(req.getImei());
//        this.setMqRouteKey(routeKey);
//        try {
//            InetAddress addr = InetAddress.getLocalHost();
//            this.setPipelineHost(addr.getHostAddress() + ":" + port);//设置本机ip
//
//        }catch (UnknownHostException e){
//            e.printStackTrace();
//        }
//    }

}
