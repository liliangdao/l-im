package com.lld.im.common.enums.command;

//2
public enum GroupEventCommand implements Command {


    /**
     * 推送申请入群通知 2023
     */
    JOIN_GROUP(2000),

    /**
     * 推送添加群成员 2001，通知给所有管理员和本人
     */
    ADDED_MEMBER(2001),

    /**
     * 推送创建群组通知 2002，通知给所有人
     */
    CREATED_GROUP(2002),

    /**
     * 推送更新群组通知 2003，通知给所有人
     */
    UPDATED_GROUP(2003),

    /**
     * 推送退出群组通知 2004，通知给管理员和操作人
     */
    EXIT_GROUP(2004),

    /**
     * 推送修改群成员通知 2005，通知给管理员和被操作人
     */
    UPDATED_MEMBER(2005),

    /**
     * 推送删除群成员通知 2006，通知给所有群成员和被踢人
     */
    DELETED_MEMBER(2006),

    /**
     * 推送解散群通知 2007，通知所有人
     */
    DESTROY_GROUP(2007),

    /**
     * 推送转让群主 2008，通知所有人
     */
    TRANSFER_GROUP(2008),

    /**
     * 禁言群 2009，通知所有人
     */
    MUTE_GROUP(2009),


    /**
     * 禁言/解禁 群成员 2010，通知管理员和被操作人
     */
    SPEAK_GOUP_MEMBER(2010),

    ;

    private Integer command;

    GroupEventCommand(int command) {
        this.command = command;
    }


    public Integer getCommand() {
        return command;
    }
}
