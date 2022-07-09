package com.lld.im.service.group.service.impl;

import com.lld.im.common.ResponseVO;
import com.lld.im.service.group.dao.mapper.ImGroupDataMapper;
import com.lld.im.service.group.model.req.CreateGroupReq;
import com.lld.im.service.group.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/9
 * @version: 1.0
 */
@Service
public class GroupServiceImpl implements GroupService {

    @Autowired
    ImGroupDataMapper imGroupDataMapper;

    @Override
    public ResponseVO createGroup(CreateGroupReq req) {



        return null;
    }
}
