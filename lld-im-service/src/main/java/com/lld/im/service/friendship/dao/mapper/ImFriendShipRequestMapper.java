package com.lld.im.service.friendship.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lld.im.service.friendship.dao.ImFriendShipRequestEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface ImFriendShipRequestMapper extends BaseMapper<ImFriendShipRequestEntity> {
    @Select(
            " select ifnull(Max(sequence),0) from im_friendship_request where app_id = #{appId} and to_id = #{userId} "
    )
    Long getFriendShipRequestMaxSeq(Integer appId, String userId);
}
