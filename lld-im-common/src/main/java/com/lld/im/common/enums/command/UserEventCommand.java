package com.lld.im.common.enums.command;

public enum UserEventCommand implements Command {

    //4000
    USER_MODIFY(4000),

    //4001
    USER_ONLINE_STATUS_CHANGE(4001),

    //4002 在线状态订阅
    USER_ONLINE_STATUS_SUBSCRIBE(4002),

    //4003 拉取订阅的在线状态好友,只发送给请求端
    PULL_USER_ONLINE_STATUS(4003),

    //4004 用户在线状态通知报文
    USER_ONLINE_STATUS_CHANGE_NOTIFY(4004),
    ;

    private Integer command;

    UserEventCommand(int command) {
        this.command = command;
    }


    public Integer getCommand() {
        return command;
    }
}
