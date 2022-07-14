package com.lld.im.service.group.service;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.group.model.req.CreateGroupReq;
import com.lld.im.service.group.model.req.DestroyGroupReq;
import com.lld.im.service.group.model.req.GetJoinedGroupReq;
import com.lld.im.service.group.model.req.UpdateGroupReq;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/9
 * @version: 1.0
 */
public interface GroupService {

    public ResponseVO createGroup(CreateGroupReq req);

    public ResponseVO updateBaseGroupInfo(UpdateGroupReq req);

    public ResponseVO getJoinedGroup(GetJoinedGroupReq req);

    public ResponseVO destroyGroup(DestroyGroupReq req);
}
