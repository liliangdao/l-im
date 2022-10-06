package com.lld.im.common.model.msg;

import com.lld.im.common.model.ClientInfo;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-10-06 09:34
 **/
@Data
public class RecallMessageContent extends ClientInfo {

    private Long messageKey;

    private String fromId;

    private String toId;

    private Long messageTime;

    private Long messageSequence;


//    {
//        "messageKey":419455774914383872,
//            "fromId":"lld",
//            "toId":"lld4",
//            "messageTime":"1665026849851",
//            "messageSequence":2,
//            "appId": 10000,
//            "clientType": 1,
//            "imei": "web"
//    }
}
