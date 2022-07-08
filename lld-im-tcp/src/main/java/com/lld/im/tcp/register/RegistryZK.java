package com.lld.im.tcp.register;

import com.lld.im.codec.config.BootstrapConfig;
import com.lld.im.common.constant.Constants;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/8
 * @version: 1.0
 */
public class RegistryZK implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(RegistryZK.class);

    private ZKit zKit;
    private String ip;
    private BootstrapConfig.TcpConfig tcpConfig;

    public RegistryZK(String ip, BootstrapConfig.TcpConfig tcpConfig) {
        this.ip = ip;
        ZkClient zkClient = new ZkClient(tcpConfig.getZkConfig().getZkAddr(), tcpConfig.getZkConfig().getZkConnectTimeOut());
        this.zKit = new ZKit(zkClient);
        this.tcpConfig = tcpConfig;

    }

    @Override
    public void run() {

        //创建父节点
        zKit.createRootNode();

        //是否要将自己注册到 ZK
        String tcpPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp + "/" + ip + ":" + tcpConfig.getTcpPort();
        zKit.createNode(tcpPath);
        logger.info("Registry zookeeper tcpPath success, msg=[{}]", tcpPath);
        if(tcpConfig.isEnableWebSocket()){
            String webPath = Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb + "/" + ip + ":" + tcpConfig.getWebSocketPort();
            zKit.createNode(webPath);
            logger.info("Registry zookeeper webPath success, msg=[{}]", webPath);
        }
    }

}
