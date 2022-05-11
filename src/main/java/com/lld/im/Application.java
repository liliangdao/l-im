package com.lld.im;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-05 10:30
 **/
@SpringBootApplication
@MapperScan(basePackages = {"com.lld.dao.mapper"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
