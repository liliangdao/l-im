package com.lld.im.common.enums.command;

public enum SystemCommand implements Command {

    //心跳
    PING(9999),

    //登陸  9000
    LOGIN(9000),

    //下线通知 用于多端互斥  9002
    MUTUALLOGIN(9002),

    //登出  9001
    LOGOUT(9001),
    ;

    private Integer command;

    SystemCommand(int command) {
        this.command = command;
    }


    public Integer getCommand() {
        return command;
    }
}
