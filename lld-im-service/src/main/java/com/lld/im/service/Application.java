package com.lld.im.service;

import com.github.jeffreyning.mybatisplus.conf.EnableKeyGen;
import com.github.jeffreyning.mybatisplus.conf.EnableMPP;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = {"com.lld.im.service", "com.lld.im.common"})
@MapperScan("com.lld.im.service.*.dao.mapper")
@EnableMPP
@EnableKeyGen
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


}


