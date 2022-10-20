package com.lld.im.tcp.reciver;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.codec.proto.MessagePack;
import com.lld.im.common.ClientType;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.DeviceMultiLoginEnum;
import com.lld.im.common.enums.command.SystemCommand;
import com.lld.im.tcp.handler.NettyServerHandler;
import com.lld.im.common.model.UserClientDto;
import com.lld.im.tcp.redis.RedisManager;
import com.lld.im.tcp.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.redisson.api.RTopic;
import org.redisson.api.listener.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author: Chackylee
 * @description: 暂时使用redis监听用户上下线消息，做互斥处理
 * 多端同步模式：1 只允许一端在线，手机/电脑/web 踢掉除了本client+imel的设备
 * 2 允许手机/电脑的一台设备 + web在线 踢掉除了本client+imel的非web端设备
 * 3 允许手机和电脑单设备 + web 同时在线 踢掉非本client+imel的同端设备
 * 4 允许所有端多设备登录 不踢任何设备
 * @create: 2022-05-10 10:53
 **/
public class UserLoginMessageListener {

    private final static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private Integer loginModel;

    public UserLoginMessageListener(Integer loginModel) {
        this.loginModel = loginModel;
    }

    /**
     * @param
     * @return void
     * @description: // * @description: 暂时使用redis监听用户上下线消息，做互斥处理
     * // *                多端同步模式：1 只允许一端在线，手机/电脑/web 踢掉除了本client+imel的设备
     * // *                            2 允许手机/电脑的一台设备 + web在线 踢掉除了本client+imel的非web端设备
     * // *                            3 允许手机和电脑单设备 + web 同时在线 踢掉非本client+imel的同端设备
     * // *                            4 允许所有端多设备登录 不踢任何设备
     * @author lld
     * @since 2022/7/9
     */
    public void listenerUserLogin() {
        RTopic topic = RedisManager.getRedissonClient().getTopic(Constants.RedisConstants.UserLoginChannel);
        topic.addListener(String.class, new MessageListener<String>() {
            @Override
            public void onMessage(CharSequence channel, String msg) {

                logger.info("收到用户上线redis消息：" + msg);
                UserClientDto userClientDto = JSONObject.parseObject(msg, UserClientDto.class);

                List<NioSocketChannel> nioSocketChannels = SessionSocketHolder.get(userClientDto.getAppId(), userClientDto.getUserId());
                for (NioSocketChannel nioSocketChannel : nioSocketChannels) {

                    if (loginModel == DeviceMultiLoginEnum.ONE.getLoginMode()) {
                        String ClientImei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientImei)).get();
                        if (!ClientImei.equals(userClientDto.getClientType() + ":" + userClientDto.getImei())) {
                            MessagePack pack = new MessagePack();
                            pack.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                            pack.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                            nioSocketChannel.writeAndFlush(pack);
                        }
                    } else if (loginModel == DeviceMultiLoginEnum.TWO.getLoginMode()) {
                        String ClientImei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientImei)).get();
                        String[] split = ClientImei.split(":");
                        Integer clientType = Integer.valueOf(split[0]);
                        if (clientType == ClientType.WEB.getCode()) {
                            return;
                        } else {
                            //踢掉除了本客户端imel的所有channel
                            if (!ClientImei.equals(userClientDto.getClientType() + ":" + userClientDto.getImei())) {
                                MessagePack msgBody = new MessagePack();
                                msgBody.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                                msgBody.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                                msgBody.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                                nioSocketChannel.writeAndFlush(msgBody);
                            }
                        }

                    } else if (loginModel == DeviceMultiLoginEnum.THREE.getLoginMode()) {
                        String ClientImei = (String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientImei)).get();
                        String[] split = ClientImei.split(":");
                        Integer clientType = Integer.valueOf(split[0]);
                        if (clientType == ClientType.WEB.getCode()) {
                            return;
                        } else {

                            Boolean isSameClient = false;
                            if ((clientType == ClientType.IOS.getCode() || clientType == ClientType.ANDROID.getCode()) && (userClientDto.getClientType() == ClientType.IOS.getCode() || userClientDto.getClientType() == ClientType.ANDROID.getCode())) {
                                isSameClient = true;
                            }

                            if ((clientType == ClientType.WINDOWS.getCode() || clientType == ClientType.MAC.getCode()) && (userClientDto.getClientType() == ClientType.WINDOWS.getCode() || userClientDto.getClientType() == ClientType.MAC.getCode())) {
                                isSameClient = true;
                            }

                            //踢掉同端的其他连接
                            if (isSameClient &&
                                    !ClientImei.equals(userClientDto.getClientType() + ":" + userClientDto.getImei())) {
                                MessagePack msgBody = new MessagePack();
                                msgBody.setToId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                                msgBody.setUserId((String) nioSocketChannel.attr(AttributeKey.valueOf(Constants.UserId)).get());
                                msgBody.setCommand(SystemCommand.MUTUALLOGIN.getCommand());
                                nioSocketChannel.writeAndFlush(msgBody);
                            }
                        }
                    } else {
                        return;
                    }

                }


            }
        });
    }

}
