package com.lld.im.utils;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public final class SpringBeanFactory implements ApplicationContextAware, BeanFactoryAware {
	private static ApplicationContext context;

	private static BeanFactory beanFactory;

	public static <T> T getBean(Class<T> c){
		return context.getBean(c);
	}


	public static <T> T getBean(String name,Class<T> clazz){
		return context.getBean(name,clazz);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}

	/**
	 * 动态解析yml的值。
	 * @param value ${}格式
	 * @return 若是解析失败或者未查找到，均返回null
	 */
	public static String resolve(String value) {
		try {
			if (beanFactory != null && beanFactory instanceof ConfigurableBeanFactory) {
				return ((ConfigurableBeanFactory) beanFactory).resolveEmbeddedValue(value);
			}
		}catch (Exception e){
		}

		return null;
	}


	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
}
