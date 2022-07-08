package com.lld.im.tcp.redis;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.config.BootstrapConfig;
import com.lld.im.codec.proto.Msg;
import com.lld.im.codec.proto.MsgBody;
import com.lld.im.codec.proto.MsgHeader;
import com.lld.im.common.ClientType;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.DeviceMultiLoginEnum;
import com.lld.im.common.enums.MsgCommand;
import com.lld.im.common.model.UserClientDto;
import com.lld.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;

import java.util.List;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-07-08 11:57
 **/
@Slf4j
public class RedisManager {

    public static RedissonClient redissonClient;

    private static Integer loginModel;

    public static void init(BootstrapConfig config) {
        try {
            loginModel = config.getLim().getLoginModel();
            // 获取客户端策略
            ClientStrategy clientStrategy = ClientFactory.getClientStrategy(config.getLim().getRedis().getMode());
            // 获取redisson客户端
            redissonClient = clientStrategy.getRedissonClient(config.getLim().getRedis());
            listenerUserLogin();
        } catch (Exception e) {
            log.error("startUp error message", e);
        }
    }

    public static RedissonClient getRedissonClient(){
        return redissonClient;
    }

    /**
     * @description: // * @description: 暂时使用redis监听用户上下线消息，做互斥处理
     * // *                多端同步模式：1 只允许一端在线，手机/电脑/web 踢掉除了本client+imel的设备
     * // *                            2 允许手机/电脑的一台设备 + web在线 踢掉除了本client+imel的非web端设备
     * // *                            3 允许手机和电脑单设备 + web 同时在线 踢掉非本client+imel的同端设备
     * // *                            4 允许所有端多设备登录 不踢任何设备
     * @param
     * @return void
     * @author lld
     * @since 2022/7/9
     */
    private static void listenerUserLogin() {
        RTopic topic = redissonClient.getTopic(Constants.RedisConstants.UserLoginChannel);
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence channel, String msg) {

                log.info("收到用户上线redis消息：" + msg);
                UserClientDto userClientDto = JSONObject.parseObject(msg, UserClientDto.class);

                List<NioSocketChannel> nioSocketChannels = SessionSocketHolder.get(userClientDto.getAppId(), userClientDto.getUserId());
                for (NioSocketChannel nioSocketChannel : nioSocketChannels) {

                    if(loginModel == DeviceMultiLoginEnum.ONE.getLoginMode()){
                        String ClientImei = (String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientImei)).get();
                        if(!ClientImei.equals(userClientDto.getClientType()+":"+userClientDto.getImei())){
                            Msg sendMsg = new Msg();
                            MsgHeader header = new MsgHeader();
                            MsgBody msgBody = new MsgBody();
                            msgBody.setToId((String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            msgBody.setUserId((String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            msgBody.setCommand(MsgCommand.MUTUALLOGIN.getCommand());
                            sendMsg.setMsgBody(msgBody);
                            sendMsg.setMsgHeader(header);
                            header.setCommand(MsgCommand.MUTUALLOGIN.getCommand());
                            nioSocketChannel.writeAndFlush(sendMsg);
                        }
                    }else if(loginModel == DeviceMultiLoginEnum.TWO.getLoginMode()){
                        String ClientImei = (String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientImei)).get();
                        String[] split = ClientImei.split(":");
                        Integer clientType = Integer.valueOf(split[0]);
                        if(clientType == ClientType.WEB.getCode()){
                            return;
                        }else{
                            //踢掉除了本客户端imel的所有channel
                            if(!ClientImei.equals(userClientDto.getClientType()+":"+userClientDto.getImei())){
                                Msg sendMsg = new Msg();
                                MsgHeader header = new MsgHeader();
                                MsgBody msgBody = new MsgBody();
                                msgBody.setToId((String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                                msgBody.setUserId((String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                                msgBody.setCommand(MsgCommand.MUTUALLOGIN.getCommand());
                                sendMsg.setMsgBody(msgBody);
                                header.setCommand(MsgCommand.MUTUALLOGIN.getCommand());
                                nioSocketChannel.writeAndFlush(sendMsg);
                            }
                        }

                    }else if(loginModel == DeviceMultiLoginEnum.THREE.getLoginMode()){
                        String ClientImei = (String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientImei)).get();
                        String[] split = ClientImei.split(":");
                        Integer clientType = Integer.valueOf(split[0]);
                        if(clientType == ClientType.WEB.getCode()){
                            return;
                        }else{

                            Boolean isSameClient = false;
                            if((clientType == ClientType.IOS.getCode() || clientType == ClientType.ANDROID.getCode()) && (userClientDto.getClientType() == ClientType.IOS.getCode() || userClientDto.getClientType() == ClientType.ANDROID.getCode())){
                                isSameClient = true;
                            }

                            if((clientType == ClientType.WINDOWS.getCode() || clientType == ClientType.MAC.getCode()) && (userClientDto.getClientType() == ClientType.WINDOWS.getCode() || userClientDto.getClientType() == ClientType.MAC.getCode())){
                                isSameClient = true;
                            }

                            //踢掉同端的其他连接
                            if(isSameClient &&
                                    !ClientImei.equals(userClientDto.getClientType()+":"+userClientDto.getImei())){
                                Msg sendMsg = new Msg();
                                MsgHeader header = new MsgHeader();
                                MsgBody msgBody = new MsgBody();
                                msgBody.setToId((String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                                msgBody.setUserId((String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                                msgBody.setCommand(MsgCommand.MUTUALLOGIN.getCommand());
                                sendMsg.setMsgBody(msgBody);
                                header.setCommand(MsgCommand.MUTUALLOGIN.getCommand());
                                nioSocketChannel.writeAndFlush(sendMsg);
                            }
                        }
                    }else {
                        return;
                    }

                }


            }
        });
    }

}
