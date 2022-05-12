package com.lld.im.utils;

import com.lld.im.constant.Constants;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

        boolean tcpExists = zkClient.exists(Constants.IMCORE_ZK_ROOT+"/tcp");
        if (!tcpExists) {
            zkClient.createPersistent(Constants.IMCORE_ZK_ROOT+"/tcp");
        }

        boolean webExists = zkClient.exists(Constants.IMCORE_ZK_ROOT+"/web");
        if (!webExists) {
            zkClient.createPersistent(Constants.IMCORE_ZK_ROOT+"/web");
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
}
