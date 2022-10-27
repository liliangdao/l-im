package com.lld.im.service.friendship.dao;

import com.baomidou.mybatisplus.annotation.*;
import com.gitee.sunchenbin.mybatis.actable.annotation.Column;
import com.gitee.sunchenbin.mybatis.actable.annotation.Table;
import com.gitee.sunchenbin.mybatis.actable.constants.MySqlTypeConstant;
import lombok.Data;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 14:32
 **/
@Data
@Table(name = "im_friendship_request")
@TableName("im_friendship_request")
public class ImFriendShipRequestEntity {

    @TableId(type = IdType.AUTO)
    @Column(name = "id",isKey = true,
            type = MySqlTypeConstant.INT, length = 20, comment = "id")
    private Long id;

    @Column(name = "app_id", type = MySqlTypeConstant.INT, length = 20, comment = "app_id")
    private Integer appId;

    @Column(name = "from_id", type = MySqlTypeConstant.VARCHAR, length = 50,
            comment = "from_id")
    private String fromId;

    @Column(name = "to_id", type = MySqlTypeConstant.VARCHAR, length = 50,
            comment = "to_id")
    private String toId;
    /** 备注*/
//    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @Column(name = "remark", type = MySqlTypeConstant.VARCHAR, length = 50,
            comment = "备注")
    private String remark;

    //是否已读 1已读
    @Column(name = "read_status", type = MySqlTypeConstant.INT, length = 10,
            comment = "是否已读 1已读")
    private Integer readStatus;

    /** 好友来源*/
//    @TableField(updateStrategy = FieldStrategy.IGNORED)
    @Column(name = "add_source", type = MySqlTypeConstant.VARCHAR, length = 20,
            comment = "好友来源")
    private String addSource;

//    @TableField(updateStrategy = FieldStrategy.IGNORED)
@Column(name = "add_wording", type = MySqlTypeConstant.VARCHAR, length = 50,
        comment = "好友验证信息")
    private String addWording;

    //审批状态 1同意 2拒绝
    @Column(name = "approve_status", type = MySqlTypeConstant.INT, length = 10,
            comment = "审批状态 1同意 2拒绝")
    private Integer approveStatus;

//    @TableField(updateStrategy = FieldStrategy.IGNORED)
@Column(name = "create_time", type = MySqlTypeConstant.BIGINT, length = 20,
        comment = "")
    private Long createTime;

    @Column(name = "update_time", type = MySqlTypeConstant.BIGINT, length = 20,
            comment = "")
    private Long updateTime;

    /** 序列号*/
    @Column(name = "sequence", type = MySqlTypeConstant.BIGINT, length = 20,
            comment = "")
    private Long sequence;




}
