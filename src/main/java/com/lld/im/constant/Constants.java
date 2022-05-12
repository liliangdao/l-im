package com.lld.im.constant;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-05 09:58
 **/
public class Constants {

    /** channel绑定的userId Key*/
    public static final String UserId = "userId";

    /** channel绑定的userId Key*/
    public static final String AppId = "appId";

    /** channel绑定的clientType 和 imel Key*/
    public static final String ClientImei = "clientImei";

    public static final String Mq2MsgService = "Pipeline2MessageService";

    public static final String IMCORE_ZK_ROOT = "/im-coreRoot";

    public static class RedisConstants{

        /**
         * 用户session
         */
        public static final String UserSessionConstants = "accountSession";

        /**
         * 用户上线通知channel
         */
        public static final String UserLoginChannel = "signal/channel/LOGIN_USER_INNER_QUEUE";

    }


}
