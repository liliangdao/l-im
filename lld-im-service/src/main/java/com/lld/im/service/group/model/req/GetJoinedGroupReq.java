package com.lld.im.service.group.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;
import sun.dc.pr.PRError;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-14 11:43
 **/
@Data
public class GetJoinedGroupReq extends RequestBase {

    @NotBlank(message = "用户id不能为空")
    private String memberId;

    //群类型
    private List<Integer> groupType;

    //单次拉取的群组数量，如果不填代表所有群组
    private Integer limit;

    //第几页
    private Integer offset;


}
