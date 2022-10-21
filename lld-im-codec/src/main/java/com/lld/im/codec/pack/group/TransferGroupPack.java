package com.lld.im.codec.pack.group;

import lombok.Data;

/**
 * @author: Chackylee
 * @description: 转让群主通知报文
 * @create: 2022-10-07 11:41
 **/
@Data
public class TransferGroupPack {

    private String groupId;

    private String ownerId;

}
