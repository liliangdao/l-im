package com.lld.im.codec.pack.message;

import lombok.Data;
import lombok.NoArgsConstructor;
import sun.dc.pr.PRError;

/**
 * @author: Chackylee
 * @description: 撤回消息通知报文
 * @create: 2022-09-09 10:15
 **/
@Data
@NoArgsConstructor
public class RecallMessageNotifyPack {

    private String fromId;

    private String toId;

    private Long messageKey;

    private Long messageSequence;
}
