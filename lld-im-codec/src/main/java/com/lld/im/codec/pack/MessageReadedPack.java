package com.lld.im.codec.pack;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-29 16:24
 **/
@Data
public class MessageReadedPack {

    private String fromId;

    private String toId;

    private Integer conversationType;

    private long messageSequence;

    private Integer appId;

}
