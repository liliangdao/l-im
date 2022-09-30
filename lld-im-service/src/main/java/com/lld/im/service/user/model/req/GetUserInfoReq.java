package com.lld.im.service.user.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-12 15:23
 **/
@Data
public class GetUserInfoReq extends RequestBase {

    private List<String> userIds;


}
