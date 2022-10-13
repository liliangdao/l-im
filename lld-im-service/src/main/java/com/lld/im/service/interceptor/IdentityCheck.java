package com.lld.im.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.BaseErrorCode;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.GateWayErrorCode;
import com.lld.im.common.enums.ImUserTypeEnum;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.common.exception.ApplicationExceptionEnum;
import com.lld.im.common.utils.TLSSigAPI;
import com.lld.im.service.user.dao.ImUserDataEntity;
import com.lld.im.service.user.service.ImUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-10-07 15:58
 **/
@Component
public class IdentityCheck {

    private static Logger logger = LoggerFactory.getLogger(IdentityCheck.class);

    @Autowired
    ImUserService imUserService;

    @Autowired
    AppConfig appConfig;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    /**
     * 根据appid,identifier判断是否App管理员,并设置到RequestHolder
     * @param identifier
     * @param appId
     * @return
     */
    public void setIsAdmin(String identifier, Integer appId) {
        //去DB或Redis中查找, 后面写
        ResponseVO<ImUserDataEntity> singleUserInfo = imUserService.getSingleUserInfo(identifier, appId);
        if(singleUserInfo.isOk()){
            RequestHolder.set(singleUserInfo.getData().getUserType() == ImUserTypeEnum.APP_ADMIN.getCode());
        }
    }

    /**
     *
     * @param identifier
     * @param appid
     * @param userSig  加密
     * @return 0 : 正常, 1: 验证失败, 2: userSig 过期, 3: appid未找到
     */
    public ApplicationExceptionEnum checkUserSig(String identifier, String appid, String userSig) {

        String cacheUserSig = stringRedisTemplate.opsForValue().get(appid + ":"
                + Constants.RedisConstants.userSign + ":" + identifier + ":" + userSig);
        if (StringUtils.isNotBlank(cacheUserSig) &&
                Long.valueOf(cacheUserSig) > System.currentTimeMillis() / 1000) {
            return BaseErrorCode.SUCCESS;
        }

        // 获取appid对应的
        String secretKey = appConfig.getPrivateKey();

        TLSSigAPI sigApi = new TLSSigAPI(Long.valueOf(appid), secretKey);
        logger.error("sigApi:appId:" + appid +",secretKey=" + secretKey);
        long expireSec = 0L;

        JSONObject sigDoc = sigApi.decodeUserSig(userSig); // 解密
        Long expireTime = 0L;
        String decodeAppId = "";
        String signIdentifier = "";
        try {
            String expire = sigDoc.get("TLS.expire").toString();
            String expireTimeStr = sigDoc.get("TLS.expireTime").toString();
            expireSec = Long.valueOf(expire);
            decodeAppId = sigDoc.get("TLS.appId").toString();
            expireTime = Long.valueOf(expireTimeStr) + Long.valueOf(expire);
            signIdentifier = sigDoc.getString("TLS.identifier");

        } catch (Exception ex){
            ex.printStackTrace();
            logger.error("checkUserSig-error:" + ex.getMessage());
        }

        if (expireTime < (System.currentTimeMillis() / 1000)) {
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;
        }

        if (expireSec == 0L)
            return GateWayErrorCode.USERSIGN_IS_EXPIRED;

        if(!signIdentifier.equals(identifier)){
            return GateWayErrorCode.USERSIGN_OPERATE_NOT_MATE;
        }

        if (!decodeAppId.equals(appid))
            return GateWayErrorCode.APPID_NOT_EXIST;

        Long etime = expireTime - System.currentTimeMillis()/1000;
        stringRedisTemplate.opsForValue().set(appid + ":"
                + Constants.RedisConstants.userSign + ":" + identifier + ":" + userSig,expireTime.toString(),etime, TimeUnit.SECONDS);

        this.setIsAdmin(identifier,Integer.valueOf(appid));
        return BaseErrorCode.SUCCESS;
    }
}
