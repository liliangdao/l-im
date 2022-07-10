package com.lld.im.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-16 11:10
 **/
@Data
@Component
@ConfigurationProperties(prefix = "appconfig")
public class AppConfig {

    /** zk连接地址*/
    private String zkAddr;

    /** zk连接超时时间*/
    private Integer zkConnectTimeOut;

    /** im管道地址路由策略*/
    private Integer imRouteWay;

    /** 如果选用一致性hash的话具体hash算法*/
    private Integer consistentHashWay;

    private Integer tcpPort;

    private Integer webSocketPort;

    private boolean needWebSocket;

//  #  *                多端同步模式：1 只允许一端在线，手机/电脑/web 踢掉除了本client+imel的设备
//  #  *                            2 允许手机/电脑的一台设备 + web在线 踢掉除了本client+imel的非web端设备
//  #  *                            3 允许手机和电脑单设备 + web 同时在线 踢掉非本client+imel的同端设备
//  #  *                            4 允许所有端多设备登录 不踢任何设备
    private Integer loginModel;

    //群成员的最大人数
    private Integer groupMaxMemberCount;

}
