package com.lld.im.common.enums.command;

public enum MessageCommand implements Command {

    //测试  8888
    TEST(8888),

    //单聊消息收发  1103
    MSG_P2P(0x44F),

    //群聊消息收发   1104
    MSG_GROUP(0x450),

    //直播群聊消息下发  1018
    MSG_GROUP_LIVE(0x3FA),

    //单聊消息收发同步消息  1108
    MSG_P2P_SYNC(0x454),

    //群聊消息收发同步消息   1109
    MSG_GROUP_SYNC(0x455),

    //发送消息已读   1106
    MSG_READED(0x452),

    //推送系统通知
    MSG_SYS(0x44C),

    //单聊消息ACK 1046
    MSG_ACK(0x416),

    //群聊消息ACK
    GROUP_MSG_ACK(0x417),

    //批量转发消息ACK
    BATCH_MSG_ACK(0x419),

    //消息已读回包  1107
    MSG_READED_ACK(0x453),

    //消息已读回包  8107
    CSCMSG_READED_ACK(0x1FAB),

    //消息撤回 1050
    MSG_RECALL(0x41A),

    //消息撤回通知 1052
    MSG_RECALL_NOTIFY(0x41C),

    //消息撤回回报 1051
    MSG_RECALL_ACK(0x41B),

    //消息已读通知 1053
    MSG_READED_NOTIFY(0x41D),

    //批量消息转发 1120
    MSG_BATCH(0x460),

    //单聊消息接收确认  1046
    MSG_P2P_RECEIVED(0x416),

    //群聊消息接收确认 1047
    MSG_GROUP_RECEIVED(0x417),

    //1060
    MSG_MODIFY(0x424),

    //1061
    MSG_MODIFY_ACK(0x425),

    //1062
    MSG_MODIFY_NOTIFY(0x426),

    //单聊消息收发同步消息接收确认 1146
    MSG_SYNC_ACK(0x47A),

    //群聊消息收发同步消息接收确认 1147
    MSG_GROUP_SYNC_ACK(0x47B),

    /**
     *会话更新推送通知 5010
     */
    CONVERSATION_CHANGE_NOTIFY(0x1392),

    /**
     * 删除会话成功TCP多端同步通知 5011
     */
    CONVERSATION_DEL(0x1393);


    private Integer command;

    MessageCommand(int command) {
        this.command = command;
    }


    public Integer getCommand() {
        return command;
    }
}
