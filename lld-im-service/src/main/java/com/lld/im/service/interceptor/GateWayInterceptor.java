package com.lld.im.service.interceptor;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.common.BaseErrorCode;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.config.AppConfig;
import com.lld.im.common.enums.GateWayErrorCode;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.common.exception.ApplicationExceptionEnum;
import com.lld.im.common.utils.TLSSigAPI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author: Chackylee
 * @description: 拦截器网关
 * @create: 2022-09-22 16:27
 **/
@Component
public class GateWayInterceptor implements HandlerInterceptor {

    @Autowired
    AppConfig appConfig;

    @Autowired
    IdentityCheck identityCheck;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

//        if (1 == 1){
//            return true;
//        }

        String appId = request.getParameter("appId");
        if(StringUtils.isBlank(appId)){
            resp(ResponseVO.errorResponse(GateWayErrorCode.APPID_NOT_EXIST),response);
            return false;
        }

        String operater = request.getParameter("identifier");
        if(StringUtils.isBlank(operater)){
            resp(ResponseVO.errorResponse(GateWayErrorCode.OPERATER_NOT_EXIST),response);
            return false;
        }

        String userSign = request.getParameter("userSign");
        if(StringUtils.isBlank(userSign)){
            resp(ResponseVO.errorResponse(GateWayErrorCode.USERSIGN_NOT_EXIST),response);
            return false;
        }

        ApplicationExceptionEnum result = identityCheck.checkUserSig(operater, appId, userSign);
        if(result.getCode() != BaseErrorCode.SUCCESS.getCode()){
            resp(ResponseVO.errorResponse(result),response);
            return false;
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        RequestHolder.remove();
    }

    private void resp(ResponseVO vo,HttpServletResponse response) throws Exception {
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        // 支持跨域
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods",
                "GET,POST,PUT,DELETE,OPTIONS");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type,X-Token");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        try {
            String resp = JSONObject.toJSONString(vo);
            writer = response.getWriter();
            writer.print(resp);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null)
                writer.close();
        }

    }
}
