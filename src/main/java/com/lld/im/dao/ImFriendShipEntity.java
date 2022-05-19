package com.lld.im.dao;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 09:24
 **/
@Data
@TableName("im_friendship")
public class ImFriendShipEntity {

    private Integer appId;

    private String fromId;

    private String toId;
    /** 备注*/
    private String remark;
    /** 状态 1正常 2删除*/
    private Integer status;
    /** 状态 1正常 2拉黑*/
    private Integer black;

    private long createTime;
    /** 序列号*/
    private long sequence;
    /** 好友来源*/
    private String addSource;

}
