package com.lld.im.codec.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-08 10:02
 **/
@Data
public class BootstrapConfig {


    public BootstrapConfig defaultConfig(){
        return null;
    }


//    @JsonProperty("lim")
    private TcpConfig lim;

    @Data
    public static class TcpConfig{
        private Integer tcpPort;// tcp 绑定的端口号

        private Integer webSocketPort; // webSocket 绑定的端口号

        private boolean enableWebSocket; //是否启用webSocket

        private Integer bossThreadSize; // boss线程 默认=1

        private Integer workThreadSize; //work线程 默认=cpu核心数+1

        private Integer businessThreadSize; // 业务线程数 默认=cpu核心数

        private boolean enableCluster;

        /**
         * redis配置
         */
        private RedisConfig redis;

        /**
         * zk配置
         */
        private zkConfig zkConfig;
    }





    @Data
    public static class zkConfig{
        /** zk连接地址*/
        private String zkAddr;

        /** zk连接超时时间*/
        private Integer zkConnectTimeOut;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClusterConfig {
        /**
         * 集群名称 需要唯一
         */
        private String node;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisConfig {

        /**
         * 单机模式：single 哨兵模式：sentinel 集群模式：cluster
         */
        private String mode;
        /**
         * 数据库
         */
        private Integer database;
        /**
         * 密码
         */
        private String password;
        /**
         * 超时时间
         */
        private Integer timeout;
        /**
         * 最小空闲数
         */
        private Integer poolMinIdle;
        /**
         * 连接超时时间(毫秒)
         */
        private Integer poolConnTimeout;
        /**
         * 连接池大小
         */
        private Integer poolSize;

        /**
         * redis单机配置
         */
        private RedisSingle single;

        /**
         * redis集群模式配置
         */
        private RedisCluster cluster;

        /**
         * redis哨兵模式配置
         */
        private RedisSentinel sentinel;

    }


    /**
     * redis单机配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisSingle {
        /**
         * 地址
         */
        private String address;
    }

    /**
     * redis集群模式配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisCluster {
        private Integer scanInterval;
        private String nodes;
        private String readMode;
        private Integer retryAttempts;
        private Integer slaveConnectionPoolSize;
        private Integer masterConnectionPoolSize;
        private Integer retryInterval;
    }

    /**
     * redis哨兵模式配置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RedisSentinel {
        private String master;
        private String nodes;
    }


}
