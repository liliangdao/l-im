package com.lld.im.common.enums.command;

//2
public enum GroupEventCommand implements Command {


    /**
     * 推送申请入群通知 2023
     */
    JOIN_GROUP(2000),

    /**
     * 推送添加群成员 2001
     */
    ADDED_MEMBER(2001),

    /**
     * 推送创建群组通知 2002
     */
    CREATED_GROUP(2002),

    /**
     * 推送更新群组通知 2003
     */
    UPDATED_GROUP(2003),

    /**
     * 推送更新群组通知 2004
     */
    EXIT_GROUP(2004),

    /**
     * 推送修改群成员通知 2005
     */
    UPDATED_MEMBER(2005),

    /**
     * 推送删除群成员通知 2006
     */
    DELETED_MEMBER(2006),

    /**
     * 推送解散群通知 2007
     */
    DESTROY_GROUP(2007),

    ;

    private Integer command;

    GroupEventCommand(int command) {
        this.command = command;
    }


    public Integer getCommand() {
        return command;
    }
}
