package com.lld.im.service.utils;

import com.lld.im.common.config.AppConfig;
import com.lld.im.common.utils.HttpRequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-13 15:56
 **/
@Component
public class CallbackService {

    private Logger logger = LoggerFactory.getLogger(CallbackService.class);

    @Autowired
    ShareThreadPool shareThreadPool;

    @Autowired
    HttpRequestUtils httpRequestUtils;

    @Autowired
    AppConfig appConfig;

    public void callback(Integer appId, String callbackCommand, String jsonBody) {
        //开启创建群之后回调
        shareThreadPool.submit(() -> {
            try {
//                    String url, Class<T> tClass, Map<String, Object> map, Map<String, Object> header, String jsonBody, String charSet
                Object o = httpRequestUtils.doPost(appConfig.getCallbackUrl(), Object.class, builderUrlParams(appId,
                        callbackCommand), jsonBody, null);
                System.out.println(o);
            } catch (Exception e) {
                logger.error("createGroupCallback 回调出现异常 ： {}", e.getMessage());
            }
        });
    }

    public Map builderUrlParams(Integer appId, String command) {
        Map map = new HashMap();
        map.put("appId", appId);
        map.put("command", command);
        return map;
    }


}
