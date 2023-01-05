package com.lld.im.service.user.model.resp;

import com.lld.im.common.model.UserSession;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author: Chackylee
 * @description:
 * @create: 2023-01-05 15:42
 **/
@Data
public class PullAllUserOnlineStatusResp {

    private Map<String,List<UserSession>> session;

    private String customText;

    private Integer customStatus;
}
