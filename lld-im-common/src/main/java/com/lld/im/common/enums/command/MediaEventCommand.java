package com.lld.im.common.enums.command;

public enum MediaEventCommand implements Command {

    //6000 向对方拨打语音
    CALL_VOICE(6000),

    //6001 向对方拨打视频
    CALL_VIDEO(6001),

    //6002 同意请求
    ACCEPT_CALL(6002),

    //6003 同步ice
    TRANSMIT_ICE(6003),

    //6004 发送offer
    TRANSMIT_OFFER(6004),

    //6005 发送ANSWER
    TRANSMIT_ANSWER(6005),

    //6006 hangup 挂断
    HANG_UP(6006),

    //6007  拒绝
    REJECT_CALL(6007),

    //6008  取消呼叫
    CANCEL_CALL(6008),


    ;

    private Integer command;

    MediaEventCommand(int command) {
        this.command = command;
    }


    public Integer getCommand() {
        return command;
    }
}
