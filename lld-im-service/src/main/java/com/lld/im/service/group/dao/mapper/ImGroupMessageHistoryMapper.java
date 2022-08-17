package com.lld.im.service.group.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lld.im.service.group.dao.ImGroupMemberEntity;
import com.lld.im.service.group.dao.ImGroupMessageHistoryEntity;
import com.lld.im.service.group.model.req.GroupMemberDto;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImGroupMessageHistoryMapper extends BaseMapper<ImGroupMessageHistoryEntity> {


}
