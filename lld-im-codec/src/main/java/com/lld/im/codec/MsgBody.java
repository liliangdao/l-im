package com.lld.im.codec;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-04-28 10:12
 **/
@Data
public class MsgBody implements Serializable {

    private String userId;
    /**
     * 应用ID
     */
    private Integer appId;

    /**
     * 接收方
     */
    private String toId;

    /**
     * 客户端标识
     */
    private int clientType;

    private int command;
    /**
     * 消息ID
     */
    private String msgId;

    /**
     * 客户端设备唯一标识
     */
    private String imei;

    /**
     * 数据解析类型 和具体业务无关，后续根据解析类型解析data数据 0x0:Json,0x1:ProtoBuf,0x2:Xml,默认:0x0
     */
    private int msgType = 0x0;

    /**
     * 业务数据对象，如果是聊天消息则不需要解析直接透传
     */
    private Object data;

}
