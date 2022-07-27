package com.lld.im.tcp.utils;

import com.lld.im.codec.config.BootstrapConfig;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-27 10:36
 **/
public class MqFactoryUtils {

    private static BootstrapConfig.Rabbitmq rabbitmq;

    public static Connection getConnection() throws IOException, TimeoutException {
        //1.创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();//MQ采用工厂模式来完成连接的创建
        //2.在工厂对象中设置连接信息(ip,port,virtualhost,username,password)
        factory.setHost(rabbitmq.getHost());//设置MQ安装的服务器ip地址
        factory.setPort(rabbitmq.getPort());//设置端口号
        factory.setVirtualHost(rabbitmq.getVirtualHost());//设置虚拟主机名称
        //MQ通过用户来管理
        factory.setUsername(rabbitmq.getUserName());//设置用户名称
        factory.setPassword(rabbitmq.getPassword());//设置用户密码
        //3.通过工厂对象获取连接
        Connection connection = factory.newConnection();
        return connection;
    }

    public synchronized static void init(BootstrapConfig.Rabbitmq rabbitmq){
        if(MqFactoryUtils.rabbitmq == null){
            MqFactoryUtils.rabbitmq = rabbitmq;
        }
    }
}
