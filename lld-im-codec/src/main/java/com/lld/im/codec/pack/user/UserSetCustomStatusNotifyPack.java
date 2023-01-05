package com.lld.im.codec.pack.user;

import lombok.Data;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
@Data
public class UserSetCustomStatusNotifyPack {

    private String userId;

    private String customText;

    private Integer customStatus;

}
