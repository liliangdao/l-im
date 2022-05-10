package com.lld.im.reciver;

import com.alibaba.fastjson.JSONObject;
import com.lld.im.constant.Constants;
import com.lld.im.enums.MsgCommand;
import com.lld.im.handler.NettyServerHandler;
import com.lld.im.model.UserClientDto;
import com.lld.im.proto.Msg;
import com.lld.im.proto.MsgBody;
import com.lld.im.proto.MsgHeader;
import com.lld.im.utils.SessionSocketHolder;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: Chackylee
 * @description: 暂时使用redis监听用户上下线消息，做互斥处理
 *                多端同步模式：1 只允许一端在线，手机/电脑/web 踢掉除了本client+imel的设备
 *                            2 允许手机/电脑的一台设备 + web在线 踢掉除了本client+imel的非web端设备
 *                            3 允许手机和电脑单设备 + web 同时在线 踢掉非本client+imel的同端设备
 *                            4 允许所有端多设备登录 不踢任何设备
 * @create: 2022-05-10 10:53
 **/
@Component
public class UserLoginMessageListener implements MessageListener {

    private final static Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private Integer loginModel = 1;

    /**
     * @description
     * @author chackylee
     * @date 2022/5/10 11:11
     * @param [message, bytes]
     * @return void
    */
    @Override
    public void onMessage(Message message, byte[] bytes) {

        String msg = new String(message.getBody());
        logger.info("收到用户上线redis消息：" + msg);
        UserClientDto userClientDto = JSONObject.parseObject(msg, UserClientDto.class);

        List<NioSocketChannel> nioSocketChannels = SessionSocketHolder.get(userClientDto.getAppId(), userClientDto.getUserId());
        for (NioSocketChannel nioSocketChannel : nioSocketChannels) {

            if(loginModel == 1){
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
            }else if(loginModel == 2){
                String ClientImei = (String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientImei)).get();
                String[] split = ClientImei.split(":");
                Integer clientType = Integer.valueOf(split[0]);
                if(clientType == 1){
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

            }else if(loginModel == 3){
                String ClientImei = (String)nioSocketChannel.attr(AttributeKey.valueOf(Constants.ClientImei)).get();
                String[] split = ClientImei.split(":");
                Integer clientType = Integer.valueOf(split[0]);
                if(clientType == 1){
                    return;
                }else{

                    Boolean isSameClient = false;
                    if((clientType == 2 || clientType == 3) && (userClientDto.getClientType() == 2 || userClientDto.getClientType() == 3)){
                        isSameClient = true;
                    }

                    if((clientType == 4 || clientType == 5) && (userClientDto.getClientType() == 4 || userClientDto.getClientType() == 5)){
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
            }else{
                return;
            }

        }



    }
}
