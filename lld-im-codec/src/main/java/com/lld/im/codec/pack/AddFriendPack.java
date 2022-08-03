package com.lld.im.codec.pack;

import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-08-02 13:46
 **/
@Data
public class AddFriendPack extends BasePack {
    private String fromId;

    /**
     * 备注
     */
    private String remark;
    private String toId;
    /**
     * 好友来源
     */
    private String addSource;
    /**
     * 添加好友时的描述信息（用于打招呼）
     */
    private String addWording;

    private Long sequence;
}
