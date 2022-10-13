package com.lld.im.service.group.model.resp;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-14 10:20
 **/
@Data
public class GetRoleInGroupResp {

    private Long groupMemberId;

    private String memberId;

    private Integer role;

    private Long speakDate;

}
