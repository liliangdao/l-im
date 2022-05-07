package com.lld.im.enums;

public enum UserPipelineConnectState implements Command {

    //心跳  9998
    ONLINE(1),

    //登陸  1000
    OFFLINE(2);

    private int command;

    UserPipelineConnectState(int command) {
        this.command = command;
    }

    public int getCommand() {
        return command;
    }
}
