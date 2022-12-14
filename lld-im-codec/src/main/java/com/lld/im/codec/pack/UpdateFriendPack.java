package com.lld.im.codec.pack;

import com.lld.im.common.model.KeyValuesBase;
import lombok.Data;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-08-02 13:50
 **/
@Data
public class UpdateFriendPack {

    public String fromId;

    private String toId;

    private String remark;

    private List<KeyValuesBase> customerItem;

    private Long sequence;



//    @Data
//    public static class UpdateItem{
//
//        private String toId;
//
//        private String remark;
//
//        private List<KeyValuesBase> customerItem;
//
//    }

}
