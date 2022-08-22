package com.lld.im.service.config;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.github.jeffreyning.mybatisplus.base.MppSqlInjector;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.enums.ImUrlRouteWayEnum;
import com.lld.im.common.enums.RouteHashMethodEnum;
import com.lld.im.common.enums.SeqMethodEnum;
import com.lld.im.common.route.RouteHandle;
import com.lld.im.common.route.algorithm.consistenthash.AbstractConsistentHash;
import com.lld.im.service.message.dao.mapper.ImMessageHistoryMapper;
import com.lld.im.service.service.seq.AbstractSeq;
import com.lld.im.service.service.seq.Seq;
import com.lld.im.service.service.seq.SnowflakeIdWorker;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.Method;
import java.util.List;


/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-16 11:00
 **/
@Configuration
public class BeanConfig  {

    private static Logger logger = LoggerFactory.getLogger(BeanConfig.class);

    @Autowired
    AppConfig appConfig;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    MppSqlInjector mppSqlInjector;

    @Bean
    public ZkClient buildZKClient() {
        return new ZkClient(appConfig.getZkAddr(), appConfig.getZkConnectTimeOut());
    }

    @Bean
    public RouteHandle buildRouteHandle() throws Exception {
        Integer imRouteWay = appConfig.getImRouteWay();
        String routeWay = "";

        ImUrlRouteWayEnum handler = ImUrlRouteWayEnum.getHandler(imRouteWay);
        routeWay = handler.getClazz();

        RouteHandle routeHandle = (RouteHandle) Class.forName(routeWay).newInstance();
        logger.info("Current route algorithm is [{}]", routeHandle.getClass().getSimpleName());
        if (handler == ImUrlRouteWayEnum.HASH) {
            //一致性 hash 算法
            Method method = Class.forName(routeWay).getMethod("setHash", AbstractConsistentHash.class);
            Integer consistentHashWay = appConfig.getConsistentHashWay();
            String hashWay = "";

            RouteHashMethodEnum hashMethodHandler = RouteHashMethodEnum.getHandler(consistentHashWay);
            hashWay = hashMethodHandler.getClazz();

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
        Class<?> clazz = Class.forName("com.lld.im.service.service.seq.GenerateySeq");
        Seq seq = (Seq) clazz.newInstance();
        Method method = clazz.getMethod("setAbstractSeq", AbstractSeq.class);
        AbstractSeq abstractSeq = (AbstractSeq)
                Class.forName(SeqMethodEnum.REDIS.getClazz()).newInstance();
        method.invoke(seq, abstractSeq);
        Method stringRedisMethod = abstractSeq.getClass().getMethod("setRedis", StringRedisTemplate.class);
        stringRedisMethod.invoke(abstractSeq, stringRedisTemplate);
        return seq;
    }

    @Bean("snowflakeSeq")
    public Seq buildSnowflakeSeq() throws Exception {
        Class<?> clazz = Class.forName("com.lld.im.service.service.seq.GenerateySeq");
        Seq seq = (Seq) clazz.newInstance();
        Method method = clazz.getMethod("setAbstractSeq", AbstractSeq.class);
        AbstractSeq abstractSeq = (AbstractSeq)
                Class.forName(SeqMethodEnum.SNOWFLAKE.getClazz()).newInstance();
        method.invoke(seq, abstractSeq);
        Method stringRedisMethod = abstractSeq.getClass().getMethod("setSnowflakeIdWorker", SnowflakeIdWorker.class);
        stringRedisMethod.invoke(abstractSeq, new SnowflakeIdWorker(1,1));
        return seq;
    }

    /**
     * 分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

}
