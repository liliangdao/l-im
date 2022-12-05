package com.lld.im.service.group.service;

import com.lld.im.codec.pack.message.ChatMessageAck;
import com.lld.im.codec.pack.group.GroupMessagePack;
import com.lld.im.common.ResponseVO;
import com.lld.im.common.constant.Constants;
import com.lld.im.common.enums.ConversationTypeEnum;
import com.lld.im.common.enums.DelFlagEnum;
import com.lld.im.common.enums.GroupMemberRoleEnum;
import com.lld.im.common.enums.command.GroupEventCommand;
import com.lld.im.common.enums.command.MessageCommand;
import com.lld.im.common.model.ClientInfo;
import com.lld.im.common.model.msg.*;
import com.lld.im.service.group.model.req.GroupMemberDto;
import com.lld.im.service.group.model.req.SendGroupMessageReq;
import com.lld.im.service.message.model.resp.SendMessageResp;
import com.lld.im.service.message.service.*;
import com.lld.im.service.service.seq.Seq;
import com.lld.im.service.user.service.ImUserService;
import com.lld.im.service.utils.UserSessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-08-17 14:16
 **/
@Service
public class GroupMessageService {

    private static Logger logger = LoggerFactory.getLogger(GroupMessageService.class);

    @Autowired
    ImUserService imUserService;

    @Autowired
    @Qualifier("redisSeq")
    Seq seq;

    @Autowired
    MessageStoreService messageStoreService;

    @Autowired
    MessageProducer messageProducer;

    @Autowired
    CheckSendMessageService checkSendMessageService;

    @Autowired
    ImGroupMemberService groupMemberService;

    @Autowired
    MessageSyncService messageSyncService;

    private final ThreadPoolExecutor threadPoolExecutor;

    {
        final AtomicInteger tNum = new AtomicInteger(0);

        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(2 << 20), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("Message-Group-Processor-" + tNum.getAndIncrement());
                return t;
            }
        });

    }

    public SendMessageResp send(SendGroupMessageReq req){

        SendMessageResp sendMessageResp = new SendMessageResp();

        GroupChatMessageContent message = new GroupChatMessageContent();
        BeanUtils.copyProperties(req,message);
        long seq = this.seq.getSeq(req.getAppId() + ":" + Constants.SeqConstants.Message);
        message.setMessageSequence(seq);

        messageStoreService.storeGroupMessage(message);

        //插入离线库redis
        OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
        BeanUtils.copyProperties(message,offlineMessageContent);
        offlineMessageContent.setConversationType(ConversationTypeEnum.GROUP.getCode());
        messageStoreService.storeGroupOffLineMessage(offlineMessageContent);

        sendMessageResp.setMessageKey(message.getMessageKey());
        sendMessageResp.setMessageTime(System.currentTimeMillis());
        sendMessageResp.setMessageId(req.getMessageId());

        dispatchMessage(message,req.getOfflinePushInfo());
        syncToSender(message,message,req.getOfflinePushInfo());

        return sendMessageResp;
    }

    public void process(GroupChatMessageContent chatMessageData) {
        long t0 = System.currentTimeMillis();
        String fromId = chatMessageData.getFromId();
        String toId = chatMessageData.getGroupId();

        ResponseVO responseVO = imServerpermissionCheck(fromId, toId, chatMessageData.getAppId());
        if (responseVO.isOk()) {
            long seq = this.seq.getSeq(chatMessageData.getAppId() + ":" +
                    Constants.SeqConstants.Message + ":" + chatMessageData.getGroupId());
            chatMessageData.setMessageSequence(seq);
            //落库+回包+分发（发送给同步端和接收方的所有端）
            threadPoolExecutor.execute(() -> {
                //插入历史库和msgBody
                messageStoreService.storeGroupMessage(chatMessageData);
                chatMessageData.setMessageKey(chatMessageData.getMessageKey());
                //回包
                ack(chatMessageData,ResponseVO.successResponse());

                List<String> groupMemberId = groupMemberService.getGroupMemberId(toId, chatMessageData.getAppId());
                chatMessageData.setMembers(groupMemberId);

                //插入离线库
                OfflineMessageContent offlineMessageContent = new OfflineMessageContent();
                offlineMessageContent.setConversationType(ConversationTypeEnum.GROUP.getCode());
                offlineMessageContent.setToId(chatMessageData.getGroupId());
                offlineMessageContent.setDelFlag(DelFlagEnum.NORMAL.getCode());
                BeanUtils.copyProperties(chatMessageData,offlineMessageContent);
                messageStoreService.storeGroupOffLineMessage(offlineMessageContent);

                //同步给发送方其他端
                syncToSender(chatMessageData,chatMessageData,chatMessageData.getOfflinePushInfo());

                //消息分发 给同步端和接收方
                dispatchMessage(chatMessageData,chatMessageData.getOfflinePushInfo());
            });
        } else {
            ack(chatMessageData, responseVO);
        }
    }

    /**
     * @param content result
     * @return void
     * @description ack回包，消息发给指定的端
     * @author chackylee
     * @date 2022/7/22 16:29
     */
    private void ack(GroupChatMessageContent content, ResponseVO result) {
        logger.debug("result = {}",result);
        logger.info("msg ack,msgId = {},msgSeq ={}，checkResult = {}", content.getMessageId(), content.getMessageSequence(), result);
        ChatMessageAck ackData = new ChatMessageAck(content.getMessageId(), content.getMessageSequence());
        result.setData(ackData);
        messageProducer.sendToUserAppointedClient(content.getFromId(), MessageCommand.GROUP_MSG_ACK, result, content);
    }

    /**
     * @description 校验群聊发送限制
     * @author chackylee
     * @date 2022/8/17 14:33
     * @return com.lld.im.common.ResponseVO
     */
    public ResponseVO imServerpermissionCheck(String fromId, String groupId, Integer appId) {

        ResponseVO checkForbidden = checkSendMessageService.checkGroup(fromId, groupId, appId);
        if (!checkForbidden.isOk()) {
            return checkForbidden;
        }

        return ResponseVO.successResponse();
    }

    private void dispatchMessage(GroupChatMessageContent messageContent, OfflinePushInfo offlinePushInfo) {

        logger.debug("dispatchMessage : {}", messageContent);
        String groupId = messageContent.getGroupId();

        GroupMessagePack groupMessageContent = new GroupMessagePack();
        BeanUtils.copyProperties(messageContent, groupMessageContent);

        ResponseVO<List<GroupMemberDto>> groupMember = groupMemberService.getGroupMember(groupId, messageContent.getAppId());
        if(groupMember.isOk()){
            List<GroupMemberDto> data = groupMember.getData();

            for(GroupMemberDto d : data){
                if(d.getMemberId().equals(messageContent.getFromId())){
                    continue;
                }

                if(d.getRole() == GroupMemberRoleEnum.LEAVE.getCode()){
                    continue;
                }

                List<ClientInfo> successResults = new ArrayList<>();
                if(messageContent.getFromId().equals(d.getMemberId())){
                    messageProducer.sendToUserExceptClient(d.getMemberId(),GroupEventCommand.MSG_GROUP,
                            groupMessageContent,messageContent);
                }else{
                    successResults = messageProducer.sendToUser(d.getMemberId()
                            , GroupEventCommand.MSG_GROUP, groupMessageContent,messageContent.getAppId());
                }

                // 如果成功的session列表中不包括手机，则需要推送离线消息。
                if (!UserSessionUtils.containMobile(successResults)) {
                    //如果接收端没有手机，则推送离线消息
//            pushService.pushOfflineInfo(offlinePushInfo, messageContent);
                }
            }
        }

    }

    private void syncToSender(GroupChatMessageContent content,ClientInfo clientInfo, OfflinePushInfo offlinePushInfo) {
        messageProducer.sendToUserExceptClient(content.getFromId(),GroupEventCommand.MSG_GROUP,content,clientInfo);
    }


}
