package com.lld.im.service.friendship.model.resp;

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: lld
 * @createDate: 2022-10-02
 * @version: 1.0
 */
@Data
public class ImportFriendShipResp {

    private List<String> errorId;

    private List<String> successId;
}
