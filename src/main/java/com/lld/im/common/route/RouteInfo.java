package com.lld.im.common.route;

/**
 * @since JDK 1.8
 */
public final class RouteInfo {

    private String ip;
    private Integer timServerPort;

    public RouteInfo(String ip, Integer timServerPort) {
        this.ip = ip;
        this.timServerPort = timServerPort;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getTimServerPort() {
        return timServerPort;
    }

    public void setTimServerPort(Integer timServerPort) {
        this.timServerPort = timServerPort;
    }

}
