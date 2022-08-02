package com.lld.im.tcp.reciver.process;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-08-02 10:47
 **/
public class ProcessFactory {

    public static MessageProcess userEventMessageProcess;
    public static MessageProcess chatMessageProcess;

    static {
        userEventMessageProcess = new UserEventMessageProcess();
        chatMessageProcess = new ChatMessageProcess();
    }

    public static MessageProcess getMessageProcess(Integer command){
        if(command.toString().startsWith("2")){
            //2开头表示是用户消息
            return userEventMessageProcess;
        }
        if(command.toString().startsWith("1")){
            //2开头表示是用户消息
            return chatMessageProcess;
        }
        return null;
    }

}
