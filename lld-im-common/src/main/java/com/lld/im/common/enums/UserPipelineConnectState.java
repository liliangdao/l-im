package com.lld.im.common.enums;

import com.lld.im.common.enums.command.Command;

public enum UserPipelineConnectState implements Command {

    //连接
    ONLINE(1),

    //离线
    OFFLINE(2);

    private Integer command;

    UserPipelineConnectState(int command) {
        this.command = command;
    }

    public Integer getCommand() {
        return command;
    }
}
