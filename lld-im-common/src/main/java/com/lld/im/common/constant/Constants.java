package com.lld.im.common.constant;

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

    public static final String ImCoreZkRoot = "/im-coreRoot";

    public static final String ImCoreZkRootTcp = "/tcp";

    public static final String ImCoreZkRootWeb = "/web";


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

    public static class SeqConstants{

        public static final String user = "userSeq";

        public static final String friendship = "friendshipSeq";

        public static final String group = "groupSeq";

        public static final String message = "messageSeq";

    }

    public static class RabbitConstants{

        public static final String Im2MsgService = "Pipeline2MessageService";

        public static final String FriendShip2Im = "friendShip2Pipeline";

    }


}
