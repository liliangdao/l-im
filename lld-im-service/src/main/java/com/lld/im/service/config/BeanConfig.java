package com.lld.im.service.config;

import com.lld.im.common.route.RouteHandle;
import com.lld.im.common.route.algorithm.consistenthash.AbstractConsistentHash;
import com.lld.im.service.service.seq.AbstractSeq;
import com.lld.im.service.service.seq.Seq;
import com.lld.im.service.service.seq.SnowflakeIdWorker;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

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

    @Autowired
    StringRedisTemplate stringRedisTemplate;

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

    @Bean("redisSeq")
    public Seq buildRedisSeq() throws Exception {
        Seq seq = (Seq) Class.forName("com.lld.im.service.service.seq.MyGenerateySeq").newInstance();
        Method method = Class.forName("com.lld.im.service.service.seq.MyGenerateySeq").getMethod("setAbstractSeq", AbstractSeq.class);
        AbstractSeq abstractSeq = (AbstractSeq)
                Class.forName("com.lld.im.service.service.seq.RedisSeq").newInstance();
        method.invoke(seq, abstractSeq);
        Method stringRedisMethod = abstractSeq.getClass().getMethod("setRedis", StringRedisTemplate.class);
        stringRedisMethod.invoke(abstractSeq, stringRedisTemplate);
        return seq;
    }

    @Bean("snowflakeSeq")
    public Seq buildSnowflakeSeq() throws Exception {
        Seq seq = (Seq) Class.forName("com.lld.im.service.service.seq.MyGenerateySeq").newInstance();
        Method method = Class.forName("com.lld.im.service.service.seq.MyGenerateySeq").getMethod("setAbstractSeq", AbstractSeq.class);
        AbstractSeq abstractSeq = (AbstractSeq)
                Class.forName("com.lld.im.service.service.seq.SnowflakeSeq").newInstance();
        method.invoke(seq, abstractSeq);
        Method stringRedisMethod = abstractSeq.getClass().getMethod("setSnowflakeIdWorker", SnowflakeIdWorker.class);
        stringRedisMethod.invoke(abstractSeq, new SnowflakeIdWorker(1,1));
        return seq;
    }
}
