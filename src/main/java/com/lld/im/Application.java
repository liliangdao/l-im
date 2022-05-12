package com.lld.im;

import com.lld.im.utils.RegistryZK;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.InetAddress;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-05 10:30
 **/
@SpringBootApplication
@MapperScan(basePackages = {"com.lld.im.dao.mapper"})
public class Application implements CommandLineRunner {

    @Value("${tcpPort}")
    private Integer tcpPort;

    @Value("${webSocketPort}")
    private Integer webSocketPort;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        //获得本机IP
        String addr = InetAddress.getLocalHost().getHostAddress();
        Thread thread = new Thread(new RegistryZK(addr, tcpPort,webSocketPort));
        thread.setName("registry-zk");
        thread.start();
    }
}
