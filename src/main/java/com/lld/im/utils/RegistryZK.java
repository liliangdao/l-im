package com.lld.im.utils;

import com.lld.im.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since JDK 1.8
 */
public class RegistryZK implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(RegistryZK.class);

    private ZKit zKit;


    private String ip;
    private int timServerPort;
    private int webSocketPort;

    public RegistryZK(String ip, int timServerPort, int webSocketPort) {
        this.ip = ip;
        this.timServerPort = timServerPort;
        this.webSocketPort = webSocketPort;
        zKit = SpringBeanFactory.getBean(ZKit.class);
    }

    @Override
    public void run() {

        //创建父节点
        zKit.createRootNode();

        //是否要将自己注册到 ZK
        String tcpPath = Constants.IMCORE_ZK_ROOT + Constants.IMCORE_ZK_TCP + "/" + ip + ":" + timServerPort;
        String webPath = Constants.IMCORE_ZK_ROOT + Constants.IMCORE_ZK_WEB + "/" + ip + ":" + webSocketPort;
        zKit.createNode(tcpPath);
        zKit.createNode(webPath);
        logger.info("Registry zookeeper tcpPath success, msg=[{}]", tcpPath);
        logger.info("Registry zookeeper webPath success, msg=[{}]", webPath);
    }
}