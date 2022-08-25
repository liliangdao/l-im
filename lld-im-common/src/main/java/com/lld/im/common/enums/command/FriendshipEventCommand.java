package com.lld.im.common.enums.command;

public enum FriendshipEventCommand implements Command {

    //3000
    //添加好友
    FRIEND_ADD(3000),

    //更新好友
    FRIEND_UPDATE(3001),

    //删除好友
    FRIEND_DELETE(3002),

    //好友申请
    FRIEND_REQUEST(3003),

    //好友申请已读
    FRIEND_REQUEST_READ(3004),

    //好友申请审批
    FRIEND_REQUEST_APPROVER(3005),

    //添加黑名单
    FRIEND_BLACK_ADD(3010),

    //移除黑名单
    FRIEND_BLACK_DELETE(3011),

    //新建好友分组
    FRIEND_GROUP_ADD(3012),

    //删除好友分组
    FRIEND_GROUP_DELETE(3013),

    //好友分组添加成员
    FRIEND_GROUP_MEMBER_ADD(3014),

    //好友分组移除成员
    FRIEND_GROUP_MEMBER_DELETE(3015),
    ;

    private Integer command;

    FriendshipEventCommand(int command) {
        this.command = command;
    }


    public Integer getCommand() {
        return command;
    }
}
