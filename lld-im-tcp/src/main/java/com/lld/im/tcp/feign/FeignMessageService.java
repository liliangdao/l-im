package com.lld.im.tcp.feign;

import com.lld.im.common.ResponseVO;
import com.lld.im.common.model.msg.CheckSendMessageReq;
import feign.Headers;
import feign.RequestLine;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author: Chackylee
 * @description:
 **/
public interface FeignMessageService {

    @Headers({"Content-Type: application/json","Accept: application/json"})
    @RequestLine("POST /message/checkSend")
    public ResponseVO checkSendMessage(@RequestBody CheckSendMessageReq o);


}
