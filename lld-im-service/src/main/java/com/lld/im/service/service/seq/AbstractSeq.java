package com.lld.im.service.service.seq;

public abstract class AbstractSeq {

    public long getSeq(String key){
        return doGetSeq(key);
    }

    protected abstract long doGetSeq(String key);


}
