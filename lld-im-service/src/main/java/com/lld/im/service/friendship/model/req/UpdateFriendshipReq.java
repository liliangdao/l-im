package com.lld.im.service.friendship.model.req;

import com.lld.im.common.model.RequestBase;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * @description:
 * @author: lld
 * @createDate: 2022/7/9
 * @version: 1.0
 */
@Data
public class UpdateFriendshipReq extends RequestBase {

    @NotBlank(message = "用户id不能为空")
    private String fromId;

    public List<UpdateItem> updateItems;

    @Data
    public static class UpdateItem{

        private String toId;

        private String remark;


    }
}
