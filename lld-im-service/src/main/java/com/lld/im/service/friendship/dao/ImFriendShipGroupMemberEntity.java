package com.lld.im.service.friendship.dao;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.github.jeffreyning.mybatisplus.anno.AutoMap;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 09:24
 **/

@Data
@TableName("im_friendship_group_member")
@AutoMap
public class ImFriendShipGroupMemberEntity {

    @TableId(value = "group_id")
    private Long groupId;

    private String toId;

}
