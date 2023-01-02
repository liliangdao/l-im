package com.lld.im.common.enums.command;

public enum FriendshipEventCommand implements Command {

    //添加好友 3000
    FRIEND_ADD(0xbb8),

    //更新好友 3001
    FRIEND_UPDATE(0xbb9),

    //删除好友 3002
    FRIEND_DELETE(0xbba),

    //好友申请 3003
    FRIEND_REQUEST(0xbbb),

    //好友申请已读 3004
    FRIEND_REQUEST_READ(0xbbc),

    //好友申请审批 3005
    FRIEND_REQUEST_APPROVER(0xbbd),

    //添加黑名单 3010
    FRIEND_BLACK_ADD(0xbc2),

    //移除黑名单 3011
    FRIEND_BLACK_DELETE(0xbc3),

    //新建好友分组 3012
    FRIEND_GROUP_ADD(0xbc4),

    //删除好友分组 3013
    FRIEND_GROUP_DELETE(0xbc5),

    //好友分组添加成员 3014
    FRIEND_GROUP_MEMBER_ADD(0xbc6),

    //好友分组移除成员 3015
    FRIEND_GROUP_MEMBER_DELETE(0xbc7),
    ;

    private Integer command;

    FriendshipEventCommand(int command) {
        this.command = command;
    }


    public Integer getCommand() {
        return command;
    }
}
