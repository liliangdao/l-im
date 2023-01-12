package com.lld.im.service.utils;



import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: Chackylee
 * @description: 消息key生成
 **/
public class MessageKeyGenerate {

    //标识从2020.1.1开始
    private static final long T202001010000 = 1577808000000L;

//    private Lock lock = new ReentrantLock();
    AtomicReference<Thread> owner = new AtomicReference<>();

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

    /**
     * ID = timestamp(43) + nodeId(6) + rotateId(15)
     */
    public synchronized long generateId() throws Exception {

//        lock.lock();
        this.lock();

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
                this.unLock();
                return generateId();
            }
        }

        id <<= nodeIdWidth;
        id += (nodeId & nodeIdMask);

        id <<= rotateIdWidth;
        id += rotateId;

//        lock.unlock();
        this.unLock();
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

    public void lock() {
        Thread cur = Thread.currentThread();
        //lock函数将owner设置为当前线程，并且预测原来的值为空。
        // unlock函数将owner设置为null，并且预测值为当前线程。
        // 当有第二个线程调用lock操作时由于owner值不为空，导致循环
        //一直被执行，直至第一个线程调用unlock函数将owner设置为null，第二个线程才能进入临界区。
        while (!owner.compareAndSet(null, cur)){
        }
    }
    public void unLock() {
        Thread cur = Thread.currentThread();
        owner.compareAndSet(cur, null);
    }

    public static void main(String[] args) throws Exception {
//        try {
//            Calendar calendar = Calendar.getInstance();
//            MessageKeyGenerate messageKeyGenerate = new MessageKeyGenerate();
//            long msgIdFromTimestamp = getMsgIdFromTimestamp(1678459712000L);
//            System.out.println(getSharding(msgIdFromTimestamp));
//        } catch (Exception e) {
//
//        }
        MessageKeyGenerate messageKeyGenerate = new MessageKeyGenerate();
        for (int i = 0; i < 2; i++) {
            long l = messageKeyGenerate.generateId();
            System.out.println(l);
        }
    }

}
