package com.lld.im.service.group.model.callback;

import lombok.Data;
import sun.dc.pr.PRError;

import java.util.List;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/16
 * @version: 1.0
 */
@Data
public class AddMemberCallback {
    private String groupId;
    private Integer groupType;
    private String operater;
    private List<String> memberId;
}
