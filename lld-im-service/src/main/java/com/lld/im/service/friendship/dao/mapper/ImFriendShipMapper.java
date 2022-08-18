package com.lld.im.service.friendship.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lld.im.service.friendship.dao.ImFriendShipEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface ImFriendShipMapper extends BaseMapper<ImFriendShipEntity> {

    @Select(
            " select friend_sequence from im_friendship where app_id = #{appId} and from_id = #{userId} "
    )
    Long getFriendShipMaxSeq(Integer appId, String userId);
}
