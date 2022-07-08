package com.lld.im.tcp;

import com.lld.im.codec.config.BootstrapConfig;
import com.lld.im.tcp.redis.ClientFactory;
import com.lld.im.tcp.redis.ClientStrategy;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/7
 * @version: 1.0
 */
@Slf4j
public class Starter {

    public static void main(String[] args) {

//        LImServer.getInstance().start();
//        LImWebSocketServer.getInstance().start();

        if (args.length > 0) {
            start(args[0]);
        } else {
            start(null);
        }
    }

    public static void start(String path) {
        BootstrapConfig appConfig;
        if (path != null) {
            Yaml yaml = new Yaml();
            try {
                InputStream inputStream = new FileInputStream(path);
                appConfig = yaml.loadAs(inputStream, BootstrapConfig.class);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        //TODO 拿到配置文件后，初始化redis，tcp服务，zk

    }

    public void initRedis(BootstrapConfig.RedisConfig config){
        ClientStrategy clientStrategy = ClientFactory.getClientStrategy(config.getMode());
        clientStrategy.getRedissonClient(config);
    }

}
