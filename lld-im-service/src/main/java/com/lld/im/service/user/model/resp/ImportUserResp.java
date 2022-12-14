package com.lld.im.service.user.model.resp;

import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-11 14:41
 **/
@Data
public class ImportUserResp {

    private List<String> successId;

    private List<String> errorId;

}
