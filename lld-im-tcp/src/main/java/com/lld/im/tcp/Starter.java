package com.lld.im.tcp;

import com.lld.im.codec.config.BootstrapConfig;
import com.lld.im.tcp.reciver.MessageServiceReciver;
import com.lld.im.tcp.redis.ClientFactory;
import com.lld.im.tcp.redis.ClientStrategy;
import com.lld.im.tcp.redis.RedisManager;
import com.lld.im.tcp.register.RegistryZK;
import com.lld.im.tcp.server.LImServer;
import com.lld.im.tcp.server.LImWebSocketServer;
import com.lld.im.tcp.utils.MqFactoryUtils;
import lombok.extern.slf4j.Slf4j;
import org.I0Itec.zkclient.ZkClient;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
        try {
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

            // 拿到配置文件后，初始化redis，tcp服务，zk , 注册redis监听
            new LImServer(appConfig.getLim()).start();
            if (appConfig.getLim().isEnableWebSocket()) {
                new LImWebSocketServer(appConfig.getLim()).start();
            }

            RedisManager.init(appConfig);

            // 初始化mq工厂
            MqFactoryUtils.init(appConfig.getLim().getRabbitmq());
            // 启动mq监听消息服务消息
            MessageServiceReciver.init(appConfig.getLim().getBrokerId());
            //注册zk
            registerZk(appConfig);

        } catch (Exception e) {
            log.error("启动失败 {}", e.getMessage());
            e.printStackTrace();
            System.exit(500);
        }
    }

    public static void registerZk(BootstrapConfig config) throws UnknownHostException {
        String addr = InetAddress.getLocalHost().getHostAddress();
        Thread thread = new Thread(new RegistryZK(addr, config.getLim()
        ));
        thread.setName("registry-zk");
        thread.start();

    }

}
