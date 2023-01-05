package com.lld.im.common.enums.command;

public enum UserEventCommand implements Command {

    //4000
    USER_MODIFY(0xfa0),

    //4001
    USER_ONLINE_STATUS_CHANGE(0xfa1),

    //4002 在线状态订阅
    USER_ONLINE_STATUS_SUBSCRIBE(0xfa2),

    //4003 拉取订阅的在线状态好友,只发送给请求端
    PULL_USER_ONLINE_STATUS(0xfa3),

    //4004 用户在线状态通知报文 用户上下线后发送通知，由USER_ONLINE_STATUS_CHANGE处理完毕后发起
    USER_ONLINE_STATUS_CHANGE_NOTIFY(0xfa4),
    ;

    private Integer command;

    UserEventCommand(int command) {
        this.command = command;
    }


    public Integer getCommand() {
        return command;
    }
}
