package com.lld.im.codec.proto;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 消息头实体类
 * @create: 2022-04-25 08:53
 **/

@Data
public class MsgHeader {
    //消息操作指令 十六进制
    private Integer command;
    private int length;
}
