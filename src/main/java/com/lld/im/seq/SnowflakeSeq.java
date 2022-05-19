package com.lld.im.seq;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 10:40
 **/
public class SnowflakeSeq extends AbstractSeq {

    private SnowflakeIdWorker snowflakeIdWorker;


    public void setSnowflakeIdWorker(SnowflakeIdWorker snowflakeIdWorker) {
        this.snowflakeIdWorker = snowflakeIdWorker;
    }

    @Override
    protected long doGetSeq(String key) {
        return snowflakeIdWorker.nextId();
    }
}
