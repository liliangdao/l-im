package com.lld.im.common.enums.command;

public enum UserEventCommand implements Command {

    //2000
    USER_MODIFY(2000),

    //2001
    USER_ONLINE_STATUS_CHANGE(2001),
    ;

    private Integer command;

    UserEventCommand(int command) {
        this.command = command;
    }


    public Integer getCommand() {
        return command;
    }
}
