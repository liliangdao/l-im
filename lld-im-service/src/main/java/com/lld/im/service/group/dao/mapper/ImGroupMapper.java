package com.lld.im.service.group.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lld.im.service.group.dao.ImGroupEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ImGroupMapper extends BaseMapper<ImGroupEntity> {

    /**
     * @description 获取加入的群的最大seq
     * @author chackylee
     * @date 2022/8/18 11:45
     * @param []
     * @return java.lang.Long
    */
    @Select(" <script> " +
            " select sequence from im_group where app_id = #{appId} and group_id in " +
            "<foreach collection=\"groupId\" index=\"index\" item=\"id\" separator=\",\" close=\")\" open=\"(\">" +
            " {id} " +
            "</foreach>" +
            " </script> ")
    Long getMemberJoinedGroupMaxSeq(Integer appId, Collection<String> groupId);

}
