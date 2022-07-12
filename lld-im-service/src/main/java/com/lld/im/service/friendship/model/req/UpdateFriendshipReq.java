package com.lld.im.service.friendship.model.req;

import com.lld.im.common.model.KeyValuesBase;
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
public class UpdateFriendshipReq {
    private Integer appId;

    @NotBlank(message = "用户id不能为空")
    private String fromId;

    @Data
    public static class updateItem{

        private String toId;

        private String remark;

        private List<KeyValuesBase> customerItem;

    }
}
