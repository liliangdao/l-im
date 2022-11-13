package com.lld.im.common.enums.command;

public enum SystemCommand implements Command {

    //心跳
    PING(0x270f),

    //登陸  9000
    LOGIN(0x2328),

    //登录ack  9001
    LOGINACK(0x2329),

    //下线通知 用于多端互斥  9002
    MUTUALLOGIN(0x232a),

    //登出  9003
    LOGOUT(0x232b),
    ;

    private Integer command;

    SystemCommand(int command) {
        this.command = command;
    }


    public Integer getCommand() {
        return command;
    }
}
