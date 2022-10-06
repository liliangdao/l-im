package com.lld.im.codec.pack;

import lombok.Data;
import lombok.NoArgsConstructor;
import sun.dc.pr.PRError;

@Data
@NoArgsConstructor
public class RecallMessageNotifyPack {

    private String fromId;

    private String toId;

    private Long messageKey;

    private Long messageSequence;
}
