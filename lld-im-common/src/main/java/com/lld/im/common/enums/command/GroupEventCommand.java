package com.lld.im.common.enums.command;

//2
public enum GroupEventCommand implements Command {


    /**
     * 推送申请入群通知 2000
     */
    JOIN_GROUP(0x7d0),

    /**
     * 推送添加群成员 2001，通知给所有管理员和本人
     */
    ADDED_MEMBER(0x7d1),

    /**
     * 推送创建群组通知 2002，通知给所有人
     */
    CREATED_GROUP(0x7d2),

    /**
     * 推送更新群组通知 2003，通知给所有人
     */
    UPDATED_GROUP(0x7d3),

    /**
     * 推送退出群组通知 2004，通知给管理员和操作人
     */
    EXIT_GROUP(0x7d4),

    /**
     * 推送修改群成员通知 2005，通知给管理员和被操作人
     */
    UPDATED_MEMBER(0x7d5),

    /**
     * 推送删除群成员通知 2006，通知给所有群成员和被踢人
     */
    DELETED_MEMBER(0x7d6),

    /**
     * 推送解散群通知 2007，通知所有人
     */
    DESTROY_GROUP(0x7d7),

    /**
     * 推送转让群主 2008，通知所有人
     */
    TRANSFER_GROUP(0x7d8),

    /**
     * 禁言群 2009，通知所有人
     */
    MUTE_GROUP(0x7d9),

    /**
     * 禁言/解禁 群成员 2010，通知管理员和被操作人
     */
    SPEAK_GOUP_MEMBER(0x7da),

    //群聊消息收发   2104
    MSG_GROUP(0x838),

    //群聊消息收发同步消息   2105
    MSG_GROUP_SYNC(0x839),

    //群聊消息ACK 2047
    GROUP_MSG_ACK(0x7ff),

    ;

    private Integer command;

    GroupEventCommand(int command) {
        this.command = command;
    }


    public Integer getCommand() {
        return command;
    }
}
