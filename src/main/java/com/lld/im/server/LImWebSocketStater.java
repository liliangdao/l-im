package com.lld.im.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-05 14:39
 **/

@Component
public class LImWebSocketStater implements ApplicationListener<ContextRefreshedEvent>  {

    @Autowired
    LImWebSocketServer lImWebSocketServer;


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            try {
//                LImWebSocketServer.getInstance().start();
                lImWebSocketServer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
