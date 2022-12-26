package com.lld.im.common.enums.command;

public enum ConversationEventCommand implements Command {

    //5000 会话删除
    CONVERSATION_DELETE(0x1388),

    //5001 会话修改
    CONVERSATION_UPDATE(0x1389),


    ;

    private Integer command;

    ConversationEventCommand(int command) {
        this.command = command;
    }


    public Integer getCommand() {
        return command;
    }
}
