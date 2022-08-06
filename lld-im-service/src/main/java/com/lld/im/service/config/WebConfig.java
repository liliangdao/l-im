package com.lld.im.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @description:
 * @author: lld
 * @createDate: 2022-08-06
 * @version: 1.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(checkExpiredInterceptor())
//                .addPathPatterns("/**")
//                .excludePathPatterns("/**/login","/**/register");
//    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600)
                .allowedHeaders("*");
    }

}
