package com.lld.im.service.config;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;
import com.github.jeffreyning.mybatisplus.base.MppSqlInjector;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.util.List;

/**
 * @description: mybatisplus 插件类,继承自联合主键插件类。如果是历史消息的mapper，添加批量插入插件类
 * @author: lld
 * @createDate: 2022/7/23
 * @version: 1.0
 */
public class EasySqlInjector extends MppSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {

        if(mapperClass.getName().equals("com.lld.im.service.message.dao.mapper.ImMessageHistoryMapper")){
            List<AbstractMethod> methodList = super.getMethodList(mapperClass);
            methodList.add(new InsertBatchSomeColumn());//insertBatchSomeColumn
            return methodList;
        }

        return super.getMethodList(mapperClass);
    }
//
    public EasySqlInjector() {
        System.out.println("EasySqlInjector");
    }
}
