package com.lld.im.tcp;

import com.lld.im.tcp.server.LImServer;
import com.lld.im.tcp.server.LImWebSocketServer;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/7
 * @version: 1.0
 */
public class Starter {

    public static void main(String[] args) {
        LImServer.getInstance().start();
        LImWebSocketServer.getInstance().start();
    }

}
