package com.lld.im.service.group.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-10-07 10:54
 **/
@Data
public class SpeaMemberReq extends RequestBase {

    @NotBlank(message = "群id不能为空")
    private String groupId;

    @NotBlank(message = "memberId不能为空")
    private String memberId;

    //禁言时间，单位毫秒
    @NotNull(message = "禁言时间不能为空")
    private Long speakDate;
}
