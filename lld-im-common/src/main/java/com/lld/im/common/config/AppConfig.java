package com.lld.im.common.config;

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

    private Integer appId;

    private String privateKey;

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

    private boolean sendMessageCheckFriend; //发送消息是否校验关系链

    private boolean sendMessageCheckBlack; //发送消息是否校验黑名单

    private String callbackUrl;//回调地址

    private boolean addFriendCallback; //添加好友之后回调开关

    private boolean modifyFriendCallback; //修改好友之后回调开关

    private boolean deleteFriendCallback; //删除好友之后回调开关

    private boolean addFriendShipBlackCallback; //添加黑名单之后回调开关

    private boolean deleteFriendShipBlackCallback; //添加黑名单之后回调开关

    private boolean createGroupCallback; //创建群聊之后回调开关

    private boolean modifyGroupCallback; //修改群聊之后回调开关

    private boolean destroyGroupCallback;//解散群聊之后回调开关


    private Integer offlineMessageCount;//离线消息存储条数

}
