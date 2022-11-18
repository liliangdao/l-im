package com.lld.im.service.group.model.callback;

import com.lld.im.service.group.model.req.GroupMemberDto;
import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/16
 * @version: 1.0
 */
@Data
public class AddMemberBeforeCallback {
    private String groupId;
    private Integer groupType;
    private String operater;
    private GroupMemberDto memberId;
}
