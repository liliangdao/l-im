package com.lld.im.config;

import com.lld.im.common.route.RouteHandle;
import com.lld.im.common.route.algorithm.consistenthash.AbstractConsistentHash;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;



/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-16 11:00
 **/
@Configuration
public class BeanConfig {

    private static Logger logger = LoggerFactory.getLogger(BeanConfig.class);

    @Autowired
    AppConfig appConfig;

    @Bean
    public ZkClient buildZKClient() {
        return new ZkClient(appConfig.getZkAddr(), appConfig.getZkConnectTimeOut());
    }

    @Bean
    public RouteHandle buildRouteHandle() throws Exception {
        Integer imRouteWay = appConfig.getImRouteWay();
        String routeWay = "";
        if(imRouteWay.equals(1)){
            routeWay = "com.lld.im.common.route.algorithm.loop.LoopHandle";
        }else if(imRouteWay.equals(2)){
            routeWay = "com.lld.im.common.route.algorithm.random.RandomHandle";
        }else if(imRouteWay.equals(3)){
            routeWay = "com.lld.im.common.route.algorithm.consistenthash.ConsistentHashHandle";
        }

        RouteHandle routeHandle = (RouteHandle) Class.forName(routeWay).newInstance();
        logger.info("Current route algorithm is [{}]", routeHandle.getClass().getSimpleName());
        if (routeWay.contains("ConsistentHash")) {
            //一致性 hash 算法
            Method method = Class.forName(routeWay).getMethod("setHash", AbstractConsistentHash.class);

            Integer consistentHashWay = appConfig.getConsistentHashWay();

            String hashWay = "";

            if(consistentHashWay.equals(1)){
                hashWay = "com.lld.im.common.route.algorithm.consistenthash.TreeMapConsistentHash"; // TreeMap
            }else if (consistentHashWay.equals(2)){
                hashWay = "com.lld.im.common.route.algorithm.consistenthash.ConsistentHashHandle"; // 自定义map
            }
            AbstractConsistentHash consistentHash = (AbstractConsistentHash)
                    Class.forName(hashWay).newInstance();
            method.invoke(routeHandle, consistentHash);
            return routeHandle;
        } else {
            return routeHandle;
        }

    }
}