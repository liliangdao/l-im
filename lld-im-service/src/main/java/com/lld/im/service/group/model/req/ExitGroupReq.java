package com.lld.im.service.group.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class ExitGroupReq extends RequestBase {

    @NotBlank(message = "群id不能为空")
    private String groupId;

}
