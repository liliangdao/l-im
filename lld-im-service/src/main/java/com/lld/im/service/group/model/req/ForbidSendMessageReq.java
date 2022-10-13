package com.lld.im.service.group.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-10-13 15:56
 **/
@Data
public class ForbidSendMessageReq extends RequestBase {

    @NotBlank(message = "groupId不能为空")
    private String groupId;

}
