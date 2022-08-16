package com.lld.im.codec.pack;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-08-16 14:27
 **/
@Data
@NoArgsConstructor
public class MessageReadedAck extends BasePack{

    private String fromId;

    private String toId;

    private int conversationType;

    private long messageSequence;
}
