package com.lld.im.service.user.service;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.pack.UserStatusChangeNotifyPack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.command.Command;
import com.lld.im.common.enums.command.UserEventCommand;
import com.lld.im.common.model.UserSession;
import com.lld.im.service.friendship.service.ImFriendShipService;
import com.lld.im.service.message.mq.ChatOperateReceiver;
import com.lld.im.service.message.service.MessageProducer;
import com.lld.im.service.user.model.UserOnlineStatusChangeContent;
import com.lld.im.service.user.model.UserOnlineStatusSubscribeContent;
import com.lld.im.service.utils.UserSessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
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
     * @description: 用户上线通知，通知给所有好友&&通知给订阅了这个用户的人
     * @author lld
     * @since 2022/9/24
     */
    public void processUserLoginNotify(UserStatusChangeNotifyPack pack) {

        String userKey = pack.getAppId()
                + ":" + Constants.RedisConstants.subscribe + ":" + pack.getUserId();
        Set<Object> keys = stringRedisTemplate.opsForHash().keys(userKey);

        List<UserSession> userSession = userSessionUtils.getUserSession(pack.getUserId(), pack.getAppId());
        pack.setClient(userSession);

        ResponseVO allFriendId = imFriendShipService.getAllFriendId(pack.getUserId(), pack.getAppId());
        if (allFriendId.isOk()) {
            LinkedHashSet<String> data = (LinkedHashSet) allFriendId.getData();
//            data = String []
            for (String fid :
                    data) {
                messageProducer.sendToUser(fid, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                        pack,pack.getAppId());
            }
        }

        for (Object key :
                keys) {
            String filed = (String) key;
            Long expire = Long.valueOf((String) stringRedisTemplate.opsForHash().get(userKey, filed));
            if (expire > 0 && expire > System.currentTimeMillis()) {
                messageProducer.sendToUser(filed, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                        pack,pack.getAppId());
            } else {
                stringRedisTemplate.opsForHash().delete(userKey, filed);
            }
        }
    }

    /**
     * @param
     * @return void
     * @description: 处理用户订阅某个用户
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
        stringRedisTemplate.opsForHash().put(userKey, content.getUserId(), subExpireTime.toString());
    }

}
