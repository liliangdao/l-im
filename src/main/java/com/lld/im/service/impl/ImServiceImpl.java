package com.lld.im.service.impl;

import com.lld.im.common.ClientType;
import com.lld.im.service.ImService;
import com.lld.im.utils.ZKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-16 10:28
 **/
@Component
public class ImServiceImpl implements ImService , CommandLineRunner {

    @Autowired
    private ZKit zkUtil;

    @Override
    public List<String> getAllImServerList(Integer clientType) {

        List<String> allNode = null;
        if(clientType == ClientType.WEB.getCode()){
            allNode = zkUtil.getAllWebNode();
        }else {
            allNode = zkUtil.getAllTcpNode();
        }
        return allNode;
    }


    @Override
    public void run(String... args) throws Exception {
    }
}
