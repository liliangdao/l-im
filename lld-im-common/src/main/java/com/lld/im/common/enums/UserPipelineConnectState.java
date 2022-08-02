package com.lld.im.common.enums;

import com.lld.im.common.enums.command.Command;

public enum UserPipelineConnectState implements Command {

    //心跳  9998
    ONLINE(1),

    //登陸  1000
    OFFLINE(2);

    private Integer command;

    UserPipelineConnectState(int command) {
        this.command = command;
    }

    public Integer getCommand() {
        return command;
    }
}
