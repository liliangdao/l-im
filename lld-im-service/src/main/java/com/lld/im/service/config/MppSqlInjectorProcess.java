package com.lld.im.service.config;

import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * @description: spring 后置处理器，兼容mybatisplus联合主键插件和批量插入插件
 * @author: lld
 * @createDate: 2022/8/22
 * @version: 1.0
 */
@Component
public class MppSqlInjectorProcess implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DefaultSqlInjector){
            bean = new EasySqlInjector();
        }
        return bean;
    }

}
