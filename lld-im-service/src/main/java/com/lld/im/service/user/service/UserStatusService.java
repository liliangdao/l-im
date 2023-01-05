package com.lld.im.service.user.service;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.pack.user.UserSetCustomStatusNotifyPack;
import com.lld.im.codec.pack.user.UserStatusChangeNotifyPack;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.command.UserEventCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.UserSession;
import com.lld.im.service.friendship.service.ImFriendShipService;
import com.lld.im.service.message.service.MessageProducer;
import com.lld.im.service.user.model.req.PullUserFriendOnlineStatusReq;
import com.lld.im.service.user.model.req.SetUserCustomerStatusReq;
import com.lld.im.service.user.model.req.SubscribeUserOnlineStatusReq;
import com.lld.im.service.user.model.UserStatusChangeNotifyContent;
import com.lld.im.service.user.model.req.PullUserOnlineStatusReq;
import com.lld.im.service.user.model.resp.PullAllUserOnlineStatusResp;
import com.lld.im.service.utils.UserSessionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

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
     * @description: 用户上/下线通知。通知自己其他端本端上线，通知给所有好友&&通知给订阅了这个用户的人
     * @author lld
     * @since 2022/9/24
     */
    public void processUserLoginNotify(UserStatusChangeNotifyContent content) {

        if(StringUtils.isNotEmpty(content.getCustomText()) && content.getCustomStatus() != null){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("customStatus",content.getCustomStatus());
            jsonObject.put("customText",content.getCustomText());
            stringRedisTemplate.opsForValue().set(content.getAppId()+":"+ Constants.RedisConstants.userCustomerStatus + ":" + content.getUserId(),
                    jsonObject.toJSONString());
        }

        List<UserSession> userSession = userSessionUtils.getUserSession(content.getUserId(), content.getAppId());
        UserStatusChangeNotifyPack pack = new UserStatusChangeNotifyPack();
        BeanUtils.copyProperties(content,pack);
        pack.setClient(userSession);

        // 发送给自己的其他端
        messageProducer.sendToUserExceptClient(content.getUserId(),UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,pack,
                new ClientInfo(content.getAppId(),content.getClientType(),content.getImei()));

        //发送给所有好友自己了
        List<String> allFriendId = imFriendShipService.getAllFriendId(content.getUserId(), content.getAppId());
        for (String fid :
                allFriendId) {
            messageProducer.sendToUser(fid, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                    pack, pack.getAppId());
        }

        //发生给临时订阅自己的用户
        String userKey = pack.getAppId()
                + ":" + Constants.RedisConstants.subscribe + ":" + pack.getUserId();
        Set<Object> keys = stringRedisTemplate.opsForHash().keys(userKey);
        for (Object key :
                keys) {
            String filed = (String) key;
            Long expire = Long.valueOf((String) stringRedisTemplate.opsForHash().get(userKey, filed));
            if (expire > 0 && expire > System.currentTimeMillis()) {
                messageProducer.sendToUser(filed, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                        pack, content.getAppId());
            } else {
                stringRedisTemplate.opsForHash().delete(userKey, filed);
            }
        }
    }

    /**
     * @param
     * @return void
     * @description: 临时订阅
     * @author lld
     */
    public void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq content) {
        String userKey = content.getAppId()
                + ":" + Constants.RedisConstants.subscribe + ":" + content.getOperater();
        Long subExpireTime = 0L;
        if (content != null && content.getSubTime() > 0) {
            subExpireTime = System.currentTimeMillis() + content.getSubTime();
        }

        for (String beSubUserId:
                content.getSubUserId()) {
            stringRedisTemplate.opsForHash().put(userKey, beSubUserId, subExpireTime.toString());
        }
    }

    /**
     * @description: 拉取用户在线状态
     * @param
     * @return void
     * @author lld 
     */
    public PullAllUserOnlineStatusResp pullAllUserOnlineStatus(PullUserOnlineStatusReq content) {
        PullAllUserOnlineStatusResp resp = new PullAllUserOnlineStatusResp();
        Map<String,List<UserSession>> result = new HashMap<>(content.getUserList().size());
        content.getUserList().forEach(e ->{
            List<UserSession> userSession = userSessionUtils.getUserSession(e, content.getAppId());
            result.put(e,userSession);
        });
        resp.setSession(result);
        return resp;
    }

    /**
     * @description: 拉取好友在线状态
     * @param
     * @return void
     * @author lld
     */
    public PullAllUserOnlineStatusResp pullFriendOnlineStatus(PullUserFriendOnlineStatusReq req) {
        PullAllUserOnlineStatusResp resp = new PullAllUserOnlineStatusResp();
        Map<String,List<UserSession>> result = new HashMap<>();
        List<String> allFriendId = imFriendShipService.getAllFriendId(req.getOperater(), req.getAppId());
        allFriendId.forEach(e ->{
            List<UserSession> userSession = userSessionUtils.getUserSession(e, req.getAppId());
            result.put(e,userSession);
        });
        resp.setSession(result);
        return resp;
    }

    /**
     * @description: 客户端主动设置自定义状态
     * @param
     * @return void
     * @author lld
     */
    public void setUserCustomerStatus(SetUserCustomerStatusReq req) {

        //修改redis中的客户端状态
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("customStatus",req.getCustomStatus());
        jsonObject.put("customText",req.getCustomText());
        stringRedisTemplate.opsForValue().set(req.getAppId()+":"+ Constants.RedisConstants.userCustomerStatus + ":" + req.getUserId(),
                jsonObject.toJSONString());

        UserSetCustomStatusNotifyPack pack = new UserSetCustomStatusNotifyPack();

        //通知给订阅了自己的人
        pack.setUserId(req.getUserId());
        pack.setCustomStatus(req.getCustomStatus());
        pack.setCustomText(req.getCustomText());

        // 发送给自己的其他端
        messageProducer.sendToUserExceptClient(req.getUserId(),UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,pack,
                new ClientInfo(req.getAppId(),req.getClientType(),req.getImei()));

        //发送给所有好友自己了
        List<String> allFriendId = imFriendShipService.getAllFriendId(req.getUserId(), req.getAppId());
        for (String fid :
                allFriendId) {
            messageProducer.sendToUser(fid, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                    pack, req.getAppId());
        }

        //发生给临时订阅自己的用户
        String userKey = req.getAppId()
                + ":" + Constants.RedisConstants.subscribe + ":" + pack.getUserId();
        Set<Object> keys = stringRedisTemplate.opsForHash().keys(userKey);
        for (Object key :
                keys) {
            String filed = (String) key;
            Long expire = Long.valueOf((String) stringRedisTemplate.opsForHash().get(userKey, filed));
            if (expire > 0 && expire > System.currentTimeMillis()) {
                messageProducer.sendToUser(filed, UserEventCommand.USER_ONLINE_STATUS_CHANGE_NOTIFY,
                        pack, req.getAppId());
            } else {
                stringRedisTemplate.opsForHash().delete(userKey, filed);
            }
        }

    }
}
