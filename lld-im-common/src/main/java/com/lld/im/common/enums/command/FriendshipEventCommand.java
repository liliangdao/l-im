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
    ;

    private Integer command;

    FriendshipEventCommand(int command) {
        this.command = command;
    }


    public Integer getCommand() {
        return command;
    }
}
