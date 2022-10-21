package com.lld.im.codec.pack.message;

import com.lld.im.codec.pack.BasePack;
import lombok.Data;

/**
 * @author: Chackylee
 * @description: 消息已读通知报文
 * @create: 2022-07-29 16:24
 **/
@Data
public class MessageReadedPack  {

    private String fromId;

    private String toId;

    private Integer conversationType;

    private long messageSequence;

}
