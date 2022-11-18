package com.lld.im.service.friendship.model.callback;


import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-11-18 09:37
 **/
@Data
public class AddFriendBeforeCallbackDto {

    private String fromId;

    /** 备注*/
    private String remark;
    private String toId;
    /** 好友来源*/
    private String addSource;
    /** 添加好友时的描述信息（用于打招呼）*/
    private String addWording;

}
