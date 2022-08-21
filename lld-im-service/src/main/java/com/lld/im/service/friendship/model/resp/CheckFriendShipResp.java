package com.lld.im.service.friendship.model.resp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/8/21
 * @version: 1.0
 */
@Data
public class CheckFriendShipResp {

    @JsonIgnore
    public static final int CheckResult_singe_Type_AWithB = 1;
    @JsonIgnore
    public static final int CheckResult_singe_Type_NoRelation = 0;
    @JsonIgnore
    public static final int CheckResult_Type_BothWay = 1;
    @JsonIgnore
    public static final int CheckResult_Both_Type_AWithB = 2;
    @JsonIgnore
    public static final int CheckResult_Both_Type_BWithA = 3;
    @JsonIgnore
    public static final int CheckResult_Both_Type_NoRelation = 4;

    private String fromId;

    private String toId;

    //校验状态，根据双向校验和单向校验有不同的status
    //单向校验：1 from添加了to，不确定to是否添加了from；0from没有添加to，也不确定to有没有添加from
    //双向校验 1 from添加了to，to也添加了from
    //        2 from添加了t0，to没有添加from
    //        3 to添加了from，from没有添加to
    //        4 双方都没有添加
    private Integer status;

}
