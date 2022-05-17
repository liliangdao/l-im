package com.lld.im.utils;

import com.alibaba.fastjson.JSON;
import com.lld.im.constant.Constants;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: Chackylee
 * @description: Zookeeper 工具
 * @create: 2022-05-12 16:39
 **/
@Component
public class ZKit {

    private static Logger logger = LoggerFactory.getLogger(ZKit.class);

    @Autowired
    private ZkClient zkClient;

    /**
     * 创建父级节点
     */
    public void createRootNode() {
        boolean exists = zkClient.exists(Constants.IMCORE_ZK_ROOT);
        if (!exists) {
            //创建 root
            zkClient.createPersistent(Constants.IMCORE_ZK_ROOT);
        }

        boolean tcpExists = zkClient.exists(Constants.IMCORE_ZK_ROOT + Constants.IMCORE_ZK_TCP);
        if (!tcpExists) {
            zkClient.createPersistent(Constants.IMCORE_ZK_ROOT + Constants.IMCORE_ZK_TCP);
        }

        boolean webExists = zkClient.exists(Constants.IMCORE_ZK_ROOT + Constants.IMCORE_ZK_WEB);
        if (!webExists) {
            zkClient.createPersistent(Constants.IMCORE_ZK_ROOT + Constants.IMCORE_ZK_WEB);
        }

    }

    /**
     * 写入指定节点 临时目录
     *
     * @param path
     */
    public void createNode(String path) {
        zkClient.createEphemeral(path);
    }

    /**
     * get all TCP server node from zookeeper
     *
     * @return
     */
    public List<String> getAllTcpNode() {
        List<String> children = zkClient.getChildren(Constants.IMCORE_ZK_ROOT + Constants.IMCORE_ZK_TCP);
//        logger.info("Query all node =[{}] success.", JSON.toJSONString(children));
        return children;
    }

    /**
     * get all WEB server node from zookeeper
     *
     * @return
     */
    public List<String> getAllWebNode() {
        List<String> children = zkClient.getChildren(Constants.IMCORE_ZK_ROOT + Constants.IMCORE_ZK_WEB);
//        logger.info("Query all node =[{}] success.", JSON.toJSONString(children));
        return children;
    }
}
