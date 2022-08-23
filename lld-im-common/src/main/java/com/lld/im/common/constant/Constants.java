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
        public static final String UserSessionConstants = ":accountSession:";

        /**
         * 用户上线通知channel
         */
        public static final String UserLoginChannel = "signal/channel/LOGIN_USER_INNER_QUEUE";


        /**
         * seq前缀
         */
        public static final String SeqPrefix = "seq";

        /**
         * 离线消息，格式： appId + :offlineMessage: + userId
         */
        public static final String offlineMessage = "offlineMessage";
    }

    public static class SeqConstants{

        public static final String User = ":userSeq";

        public static final String Friendship = "friendshipSeq";

        public static final String FriendshipBlack = "friendshipBlackSeq";

        public static final String FriendshipRequest = "friendshipRequestSeq";

        public static final String Group = "groupSeq";

        public static final String Message = "messageSeq";

        public static final String Conversation = "conversationSeq";

    }

    public static class RabbitConstants{

        public static final String Im2MessageService = "pipeline2MessageService";

        public static final String Im2GroupService = "pipeline2GroupService";

        public static final String MessageService2Im = "messageService2Pipeline";

        public static final String FriendShip2Im = "friendShip2Pipeline";

    }

    public static class CallbackCommand{

        public static final String CreateGroup = "group.create";

        public static final String UpdateGroup = "group.update";

        public static final String DestoryGroup = "group.destory";

        public static final String GroupMemberAdd = "group.member.add";

        public static final String AddFriend = "friend.add";

        public static final String UpdateFriend = "friend.update";

        public static final String DeleteFriend = "friend.delete";

        public static final String AddBlack = "black.add";

        public static final String DeleteBlack = "black.delete";
    }


}
