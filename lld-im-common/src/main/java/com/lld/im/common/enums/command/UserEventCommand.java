package com.lld.im.common.enums.command;

public enum UserEventCommand implements Command {

    //2000
    USER_MODIFY(2000);

    private Integer command;

    UserEventCommand(int command) {
        this.command = command;
    }


    public Integer getCommand() {
        return command;
    }
}
