package com.lld.im.codec.pack;

import lombok.Data;


/**
 * @author: Chackylee
 * @description: 修改好友通知报文
 * @create: 2022-08-02 13:50
 **/
@Data
public class UpdateFriendPack {

    public String fromId;

    private String toId;

    private String remark;


    private Long sequence;


}
