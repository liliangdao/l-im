package com.lld.im.tcp.register;

import com.lld.im.common.constant.Constants;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/8
 * @version: 1.0
 */
public class ZKit {

    private static Logger logger = LoggerFactory.getLogger(ZKit.class);

    ZkClient zkClient;

    public ZKit(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    /**
     * 创建父级节点
     */
    public void createRootNode() {
        boolean exists = zkClient.exists(Constants.ImCoreZkRoot);
        if (!exists) {
            //创建 root
            zkClient.createPersistent(Constants.ImCoreZkRoot);
        }

        boolean tcpExists = zkClient.exists(Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp);
        if (!tcpExists) {
            zkClient.createPersistent(Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp);
        }

        boolean webExists = zkClient.exists(Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb);
        if (!webExists) {
            zkClient.createPersistent(Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb);
        }

    }

    /**
     * 写入指定节点 临时目录
     *
     * @param path
     */
    public void createNode(String path) {
        if(!zkClient.exists(path))
        zkClient.createEphemeral(path);
    }

    /**
     * get all TCP server node from zookeeper
     *
     * @return
     */
    public List<String> getAllTcpNode() {
        List<String> children = zkClient.getChildren(Constants.ImCoreZkRoot + Constants.ImCoreZkRootTcp);
//        logger.info("Query all node =[{}] success.", JSON.toJSONString(children));
        return children;
    }

    /**
     * get all WEB server node from zookeeper
     *
     * @return
     */
    public List<String> getAllWebNode() {
        List<String> children = zkClient.getChildren(Constants.ImCoreZkRoot + Constants.ImCoreZkRootWeb);
//        logger.info("Query all node =[{}] success.", JSON.toJSONString(children));
        return children;
    }

}
