package com.lld.im.codec.pack;

import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description: 删除好友通知报文
 * @create: 2022-08-02 13:46
 **/
@Data
public class DeleteFriendPack {

    private String fromId;

    private String toId;
}
