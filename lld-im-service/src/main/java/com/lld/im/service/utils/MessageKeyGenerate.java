package com.lld.im.service.utils;


import com.lld.im.codec.proto.Message;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: Chackylee
 * @description: 消息key生成
 **/
public class MessageKeyGenerate {

    //标识从2020.1.1开始
    private static final long T202001010000 = 1577808000000L;

    private Lock lock = new ReentrantLock();

    private static volatile int rotateId = 0;
    private static int rotateIdWidth = 15;
    private static int rotateIdMask = 32767;

    private static volatile long timeId = 0;

    private int nodeId = 0;
    private static int nodeIdWidth = 6;
    private static int nodeIdMask = 63;


    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public synchronized long generateId() throws Exception {

        lock.lock();
        rotateId = rotateId + 1;

        long id = System.currentTimeMillis() - T202001010000;

        //不同毫秒数生成的id要重置timeId和自选次数
        if (id > timeId) {
            timeId = id;
            rotateId = 1;
        } else if (id == timeId) {
            //表示是同一毫秒的请求
            if (rotateId == rotateIdMask) {
                //一毫秒只能发送32768到这里表示当前毫秒数已经超过了
                while (id <= timeId) {
                    //重新给id赋值
                    id = System.currentTimeMillis() - T202001010000;
                }
                lock.unlock();
                return generateId();
            }
        }

        id <<= nodeIdWidth;
        id += (nodeId & nodeIdMask);

        id <<= rotateIdWidth;
        id += rotateId;

        lock.unlock();
        return id;
    }

    public static int getSharding(long mid) {

        Calendar calendar = Calendar.getInstance();

        mid >>= nodeIdWidth;
        mid >>= rotateIdWidth;

        calendar.setTime(new Date(T202001010000 + mid));

        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        year %= 4;
        return (year * 12 + month);
    }

    public static long getMsgIdFromTimestamp(long timestamp) {
        long id = timestamp - T202001010000;

        id <<= rotateIdWidth;
        id <<= nodeIdWidth;

        return id;
    }

    public static void main(String[] args) {
        try {
            Calendar calendar = Calendar.getInstance();
            MessageKeyGenerate messageKeyGenerate = new MessageKeyGenerate();
            long msgIdFromTimestamp = getMsgIdFromTimestamp(1678459712000L);
            System.out.println(getSharding(msgIdFromTimestamp));
        } catch (Exception e) {

        }
    }

}
