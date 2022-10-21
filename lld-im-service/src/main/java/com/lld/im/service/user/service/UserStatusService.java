package com.lld.im.service.user.service;

import com.lld.im.codec.pack.user.UserStatusChangeNotifyPack;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.command.UserEventCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.UserSession;
import com.lld.im.service.friendship.service.ImFriendShipService;
import com.lld.im.service.message.service.MessageProducer;
import com.lld.im.service.user.model.UserOnlineStatusSubscribeContent;
import com.lld.im.service.user.model.UserStatusChangeNotifyContent;
import com.lld.im.service.user.model.req.PullUserOnlineStatusReq;
import com.lld.im.service.utils.UserSessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @description:
 * @author: lld
 * @createDate: 2022-09-23
 * @version: 1.0
 */
@Service
public class UserStatusService {

    private static Logger logger = LoggerFactory.getLogger(UserStatusService.class);

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ImFriendShipService imFriendShipService;

    @Autowired
    UserSessionUtils userSessionUtils;

    @Autowired
    MessageProducer messageProducer;

    /**
     * @param
     * @return void
     * @description: 用户上线通知。通知自己其他端本端上线，通知给所有好友&&通知给订阅了这个用户的人
     * @author lld
     * @since 2022/9/24
     */
    public void processUserLoginNotify(UserStatusChangeNotifyContent pack) {

        String userKey = pack.getAppId()
                + ":" + Constants.RedisConstants.subscribe + ":" + pack.getUserId();
        Set<Object> keys = stringRedisTemplate.opsForHash().keys(userKey);

        List<UserSession> userSession = userSessionUtils.getUserSession(pack.getUserId(), pack.getAppId());
        pack.setClient(userSession);

        // 发送给自己的其他端
        messageProducer.sendToUserExceptClient(pack.getUserId(),UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,pack,
                new ClientInfo(pack.getAppId(),pack.getClientType(),pack.getImei()));

        //发送给所有好友自己了
        List<String> allFriendId = imFriendShipService.getAllFriendId(pack.getUserId(), pack.getAppId());
        for (String fid :
                allFriendId) {
            messageProducer.sendToUser(fid, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                    pack, pack.getAppId());
        }

        //发生给临时订阅自己的用户
        for (Object key :
                keys) {
            String filed = (String) key;
            Long expire = Long.valueOf((String) stringRedisTemplate.opsForHash().get(userKey, filed));
            if (expire > 0 && expire > System.currentTimeMillis()) {
                messageProducer.sendToUser(filed, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                        pack, pack.getAppId());
            } else {
                stringRedisTemplate.opsForHash().delete(userKey, filed);
            }
        }
    }

    /**
     * @param
     * @return void
     * @description: 处理用户订阅某个用户，临时订阅
     * @author lld
     * @since 2022/9/24
     */
    public void processUserSubscribeNotify(UserOnlineStatusSubscribeContent content) {
        String userKey = content.getAppId()
                + ":" + Constants.RedisConstants.subscribe + ":" + content.getBeSubUserId();
        Long subExpireTime = 0L;
        if (content != null && content.getSubTime() > 0) {
            subExpireTime = System.currentTimeMillis() + content.getSubTime();
        }

        for (String beSubUserId:
                content.getBeSubUserId()) {
            stringRedisTemplate.opsForHash().put(userKey, beSubUserId, subExpireTime.toString());
        }
    }

    /**
     * @description: 拉取所有订阅的用户在线状态（暂时只拉取所有好友的【持久订阅的】）
     * @param
     * @return void
     * @author lld 
     * @since 2022-09-25
     */
    public List<List<UserSession>> pullAllUserOnlineStatus(PullUserOnlineStatusReq content) {

        List<List<UserSession>> result = new ArrayList<>();
        content.getUserList().forEach(e ->{
            List<UserSession> userSession = userSessionUtils.getUserSession(e, content.getAppId());
            result.add(userSession);
        });
        return result;

    }

}
