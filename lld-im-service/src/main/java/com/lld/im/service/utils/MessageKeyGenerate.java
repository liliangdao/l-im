package com.lld.im.service.utils;


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



    public void main(String[] args) throws Exception {
//        String messageTable = getMessageTable(317746708379336705L);
//        System.out.println(messageTable);
//        int hashId = Math.abs("kojqmws2k".hashCode())%128;
//        System.out.println(hashId);

        ConcurrentHashMap<Long, Integer> messageIds = new ConcurrentHashMap<>();

        int threadCount = 1000;
        int loop = 1000000;
        for (int i = 0; i < threadCount; i++) {
            new Thread(()->{
                for (int j = 0; j < loop; j++) {
                    try {
                        long mid = generateId();
                        if(messageIds.put(mid, j) != null) {
                            System.out.println("Duplicated message id !!!!!!" + mid);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        Thread.sleep(1000 * 60 * 10);
    }

}
