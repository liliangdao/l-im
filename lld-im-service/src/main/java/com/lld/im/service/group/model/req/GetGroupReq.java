package com.lld.im.service.group.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-12 14:53
 **/
@Data
public class GetGroupReq extends RequestBase {

    private List<String> groupId;

}
