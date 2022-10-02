package com.lld.im.service.friendship.model.req;

import com.lld.im.common.model.RequestBase;
import com.lld.im.service.friendship.dao.ImFriendShipEntity;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @description:
 * @author: lld
 * @createDate: 2022-10-02
 * @version: 1.0
 */
@Data
public class ImportFriendShipReq extends RequestBase {

    @NotBlank(message = "fromId不能为空")
    private String fromId;

    @NotEmpty(message = "关系链数据不能为空")
    private List<ImportFriendDto> friendItem;

    @Data
    public static class ImportFriendDto{
        /** 备注*/
        private String remark;
        private String toId;
        /** 好友来源*/
        private String addSource;
        /** 添加好友时的描述信息（用于打招呼）*/
        private String addWording;
        /** 好友状态*/
        private Integer status;
        /** 黑名单状态 */
        private Integer black;

    }

}
