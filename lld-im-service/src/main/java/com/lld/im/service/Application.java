package com.lld.im.service;

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
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


}


@RestController
class callback {
    @RequestMapping("/callback")
    public void callback(@RequestBody Object o, String command) {
        System.out.println(command);
        System.out.println(o.toString());
    }
}

