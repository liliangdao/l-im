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

    private Long messageRecallTimeOut;//#消息可撤回时间 单位毫秒

    //群成员的最大人数
    private Integer groupMaxMemberCount;

    private boolean sendMessageCheckFriend; //发送消息是否校验关系链

    private boolean sendMessageCheckBlack; //发送消息是否校验黑名单

    private String callbackUrl;//回调地址

    private boolean addFriendAfterCallback; //添加好友之后回调开关

    private boolean addFriendBeforeCallback; //添加好友之前回调开关

    private boolean modifyFriendAfterCallback; //修改好友之后回调开关

    private boolean deleteFriendAfterCallback; //删除好友之后回调开关

    private boolean addFriendShipBlackAfterCallback; //添加黑名单之后回调开关

    private boolean deleteFriendShipBlackAfterCallback; //添加黑名单之后回调开关

    private boolean createGroupAfterCallback; //创建群聊之后回调开关

    private boolean modifyGroupAfterCallback; //修改群聊之后回调开关

    private boolean destroyGroupAfterCallback;//解散群聊之后回调开关

    private boolean deleteGroupMemberAfterCallback;//删除群成员之后回调

    private boolean addGroupMemberBeforeCallback;//拉人入群之前回调

    private boolean addGroupMemberAfterCallback;//拉人入群之后回调

    private Integer offlineMessageCount;//离线消息存储条数



}
