package com.lld.im.service.config;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;
import com.github.jeffreyning.mybatisplus.base.MppSqlInjector;
import com.lld.im.service.message.dao.mapper.ImMessageHistoryMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/8/22
 * @version: 1.0
 */
@Component
public class MppSqlInjectorProcess implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DefaultSqlInjector){
            System.out.println(bean.getClass().getName());
            bean = (DefaultSqlInjector) bean;
            ((MppSqlInjector) bean).getMethodList(ImMessageHistoryMapper.class).add(new InsertBatchSomeColumn());
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        if (bean instanceof DefaultSqlInjector){
            System.out.println(bean.getClass().getName());
            bean = (DefaultSqlInjector) bean;
            ((MppSqlInjector) bean).getMethodList(ImMessageHistoryMapper.class).add(new InsertBatchSomeColumn());
        }
        return bean;
    }
}
