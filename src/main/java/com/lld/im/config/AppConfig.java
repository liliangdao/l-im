package com.lld.im.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-16 11:10
 **/

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

    public String getZkAddr() {
        return zkAddr;
    }

    public void setZkAddr(String zkAddr) {
        this.zkAddr = zkAddr;
    }

    public Integer getZkConnectTimeOut() {
        return zkConnectTimeOut;
    }

    public void setZkConnectTimeOut(Integer zkConnectTimeOut) {
        this.zkConnectTimeOut = zkConnectTimeOut;
    }

    public Integer getImRouteWay() {
        return imRouteWay;
    }

    public void setImRouteWay(Integer imRouteWay) {
        this.imRouteWay = imRouteWay;
    }

    public Integer getConsistentHashWay() {
        return consistentHashWay;
    }

    public void setConsistentHashWay(Integer consistentHashWay) {
        this.consistentHashWay = consistentHashWay;
    }
}
