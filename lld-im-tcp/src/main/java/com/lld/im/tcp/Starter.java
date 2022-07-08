package com.lld.im.tcp;

import com.lld.im.codec.config.BootstrapConfig;
import com.lld.im.tcp.redis.ClientFactory;
import com.lld.im.tcp.redis.ClientStrategy;
import com.lld.im.tcp.redis.RedisManager;
import com.lld.im.tcp.server.LImServer;
import com.lld.im.tcp.server.LImWebSocketServer;
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

        BootstrapConfig appConfig = null;
        if (path != null) {
            Yaml yaml = new Yaml();
            try {
                InputStream inputStream = new FileInputStream(path);
                appConfig = yaml.loadAs(inputStream, BootstrapConfig.class);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        //TODO 拿到配置文件后，初始化redis，tcp服务，zk , 注册redis监听
        RedisManager.init(appConfig);

        new LImServer(appConfig.getLim()).start();
        if(appConfig.getLim().isEnableWebSocket()){
            new LImWebSocketServer(appConfig.getLim()).start();
        }

        //注册redis监听
        RedisManager.init(appConfig);

        //注册zk


    }

    public static void initRedis(BootstrapConfig.RedisConfig config){
        ClientStrategy clientStrategy = ClientFactory.getClientStrategy(config.getMode());
        clientStrategy.getRedissonClient(config);
    }

}
