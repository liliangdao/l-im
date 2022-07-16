package com.lld.im.service.friendship.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/16
 * @version: 1.0
 */
@Data
public class GetRelationReq extends RequestBase {

    private String fromId;

    private Integer toId;
}
