package com.lld.im.codec.proto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Chackylee
 * @description: 消息实体
 * @create: 2022-04-25 09:02
 **/
@Data
@NoArgsConstructor
public class Message {

    private MessageHeader messageHeader;

    private Object messagePack;

}
