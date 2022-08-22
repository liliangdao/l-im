package com.lld.im.service.config;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;
import com.github.jeffreyning.mybatisplus.base.MppSqlInjector;
import org.apache.ibatis.builder.MapperBuilderAssistant;

import java.util.List;

/**
 * @description: mybatisplus 插件类
 * @author: lld
 * @createDate: 2022/7/23
 * @version: 1.0
 */
public class EasySqlInjector implements ISqlInjector {


    @Override
    public void inspectInject(MapperBuilderAssistant builderAssistant, Class<?> mapperClass) {

    }
}
