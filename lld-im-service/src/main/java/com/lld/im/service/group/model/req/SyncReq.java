package com.lld.im.service.group.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-08-18 09:56
 **/
@Data
public class SyncReq extends RequestBase {

    //客户端最大seq
    private Long lastSequence;
    //一次拉取多少
    private Integer maxLimit;

}
