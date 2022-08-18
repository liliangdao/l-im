package com.lld.im.service.group.model.resp;

import com.lld.im.service.group.dao.ImGroupEntity;
import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-08-18 11:41
 **/
@Data
public class SyncJoinedGroupResp {

    private Long maxSequence;

    private boolean isCompleted;

    private List<ImGroupEntity> dataList;

}
