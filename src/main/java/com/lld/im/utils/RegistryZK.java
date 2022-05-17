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
    private int tcpServerPort;
    private int webSocketPort;

    private boolean needWebSocket;

    public RegistryZK(String ip, int tcpServerPort, int webSocketPort,boolean needWebSocket) {
        this.ip = ip;
        this.tcpServerPort = tcpServerPort;
        this.webSocketPort = webSocketPort;
        this.needWebSocket = needWebSocket;
        zKit = SpringBeanFactory.getBean(ZKit.class);
    }

    @Override
    public void run() {

        //创建父节点
        zKit.createRootNode();

        //是否要将自己注册到 ZK
        String tcpPath = Constants.IMCORE_ZK_ROOT + Constants.IMCORE_ZK_TCP + "/" + ip + ":" + tcpServerPort;
        zKit.createNode(tcpPath);
        logger.info("Registry zookeeper tcpPath success, msg=[{}]", tcpPath);
        if(this.needWebSocket){
            String webPath = Constants.IMCORE_ZK_ROOT + Constants.IMCORE_ZK_WEB + "/" + ip + ":" + webSocketPort;
            zKit.createNode(webPath);
            logger.info("Registry zookeeper webPath success, msg=[{}]", webPath);
        }
    }
}