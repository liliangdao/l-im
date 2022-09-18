package com.lld.im.common.model.msg;

import com.lld.im.common.model.ClientInfo;
import lombok.Data;

/**
 * @author: Chackylee
 * @description: 消息接收方ack给消息发送方表示收到消息
 * @create: 2022-08-19 10:35
 **/
@Data
public class MessageReciveAckContent extends ClientInfo {

    //原消息的接收方
    private String fromId;
    //原消息的发送方
    private String toId;

    private Integer conversationType;

    private String messageId;

    private String messageKey;

}
