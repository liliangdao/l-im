package com.lld.im.service.user.model.req;

import com.lld.im.common.model.RequestBase;
import com.lld.im.service.user.dao.ImUserDataEntity;
import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description: 导入用户
 * @create: 2022-05-11 14:28
 **/
@Data
public class ImportUserReq extends RequestBase {

    private List<ImUserDataEntity> userData;


}
