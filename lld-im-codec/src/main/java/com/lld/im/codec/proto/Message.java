package com.lld.im.codec.proto;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 消息实体
 * @create: 2022-04-25 09:02
 **/
@Data
public class Message {

    private MessageHeader messageHeader;

    private MessagePack messagePack;

}
