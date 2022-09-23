package com.lld.im.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.BaseErrorCode;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.enums.GateWayErrorCode;
import com.lld.im.common.exception.ApplicationExceptionEnum;
import com.lld.im.common.utils.TLSSigAPI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-09-22 16:43
 **/
@Service
public class UserSignCheck {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    AppConfig appConfig;

    /**
     *
     * @param identifier
     * @param appid
     * @param userSig  加密
     * @return 0 : 正常, 1: 验证失败, 2: userSig 过期, 3: appid未找到
     */
    public ApplicationExceptionEnum checkUserSig(String operater, String appid, String userSig) {

        //
        String redisSign = stringRedisTemplate.opsForValue().get("");
        if (redisSign.toLowerCase().equals(userSig.toLowerCase())) {
            return BaseErrorCode.SUCCESS;
        }

        String privateKey = appConfig.getPrivateKey();

        TLSSigAPI sigApi = new TLSSigAPI(Long.valueOf(appid), privateKey);
        long expireSec = 0L;

        JSONObject sigDoc = sigApi.decodeUserSig(userSig); // 解密
        Long expireTime = 0L;
        String decodeAppId = "";
        try {
            String expire = sigDoc.get("TLS.expire").toString();
            String expireTimeStr = sigDoc.get("TLS.expireTime").toString();
            expireSec = Long.valueOf(expire);
            decodeAppId = sigDoc.get("TLS.appid").toString();
            expireTime = Long.valueOf(expireTimeStr);
        } catch (Exception ex){
//            logger.error("checkUserSig-error:" + ex.getMessage());
        }

        if (expireTime < (System.currentTimeMillis() / 1000)) {
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }

        if (expireSec == 0L)
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;

        if (!decodeAppId.equals(appid))
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;

            // 写入redis，免得多次解压
//        appManager.saveUserSigRedis(identifier, appid, userSig, expireSec);

        return BaseErrorCode.SUCCESS;
    }


}
