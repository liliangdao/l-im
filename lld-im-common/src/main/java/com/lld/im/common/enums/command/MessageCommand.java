package com.lld.im.common.enums.command;

public enum MessageCommand implements Command {

    //测试  8888
    TEST(8888),

    //单聊消息收发  1103
    MSG_P2P(0x44F),

    //单聊消息收发同步消息  1108
    MSG_P2P_SYNC(0x454),

    //发送消息已读   1106
    MSG_READED(0x452),

    //消息收到ack
    MSG_RECIVE_ACK(1107),

    //单聊消息ACK 1046
    MSG_ACK(0x416),

    //消息撤回 1050
    MSG_RECALL(0x41A),

    //消息撤回通知 1052
    MSG_RECALL_NOTIFY(0x41C),

    //消息撤回回报 1051
    MSG_RECALL_ACK(0x41B),

    //消息已读通知 1053
    MSG_READED_NOTIFY(0x41D),



   ;


    private Integer command;

    MessageCommand(int command) {
        this.command = command;
    }


    public Integer getCommand() {
        return command;
    }
}
