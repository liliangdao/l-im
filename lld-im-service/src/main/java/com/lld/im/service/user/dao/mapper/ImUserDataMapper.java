package com.lld.im.service.user.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lld.im.service.user.dao.ImUserDataEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
public interface ImUserDataMapper extends BaseMapper<ImUserDataEntity> {
}
