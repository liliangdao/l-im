package com.lld.message.config;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;

import java.util.List;

public class EasySqlInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        // TODO Auto-generated method stub
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        methodList.add(new InsertBatchSomeColumn()); // 添加InsertBatchSomeColumn方法
        return methodList;
    }

}

/**
 * @description: spring 后置处理器，兼容mybatisplus联合主键插件和批量插入插件
 * @author: lld
 * @createDate: 2022/8/22
 * @version: 1.0
 */
//@Component
//public class EasySqlInjectorProcess implements BeanPostProcessor {
//
//    @Override
//    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
//        if (bean instanceof DefaultSqlInjector){
//            bean = new EasySqlInjector();
//        }
//        return bean;
//    }
//
//}

//class EasySqlInjector extends MppSqlInjector {
//
//    @Override
//    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
//
//        if(mapperClass.getName().equals("com.lld.im.service.message.dao.mapper.ImMessageHistoryMapper")){
//            List<AbstractMethod> methodList = super.getMethodList(mapperClass);
//            methodList.add(new InsertBatchSomeColumn());//insertBatchSomeColumn
//            return methodList;
//        }
//
//        return super.getMethodList(mapperClass);
//    }
//}
