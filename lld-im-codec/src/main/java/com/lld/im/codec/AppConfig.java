package com.lld.im.codec;


/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-16 11:10
 **/
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

    private Integer loginModel;

    public Integer getLoginModel() {
        return loginModel;
    }

    public void setLoginModel(Integer loginModel) {
        this.loginModel = loginModel;
    }

    public Boolean getNeedWebSocket() {
        return needWebSocket;
    }

    public void setNeedWebSocket(Boolean needWebSocket) {
        this.needWebSocket = needWebSocket;
    }

    public Integer getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(Integer tcpPort) {
        this.tcpPort = tcpPort;
    }

    public Integer getWebSocketPort() {
        return webSocketPort;
    }

    public void setWebSocketPort(Integer webSocketPort) {
        this.webSocketPort = webSocketPort;
    }

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
