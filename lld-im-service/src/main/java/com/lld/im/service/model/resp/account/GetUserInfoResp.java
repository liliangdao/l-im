package com.lld.im.service.model.resp.account;

import com.lld.im.service.dao.ImUserDataEntity;
import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-12 15:28
 **/
@Data
public class GetUserInfoResp {

    private List<ImUserDataEntity> UserDataItem;

    private List<String> failUser;


}
