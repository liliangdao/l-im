package com.lld.im.service.friendship.model.req;

import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 09:20
 **/
@Data
public class FriendDto {

    /** 备注*/
    private String remark;
    private String toId;
    /** 好友来源*/
    private String addSource;
    /** 添加好友时的描述信息（用于打招呼）*/
    private String addWording;

}
