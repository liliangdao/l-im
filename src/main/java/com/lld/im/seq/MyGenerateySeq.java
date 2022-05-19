package com.lld.im.seq;

/**
 * @author: Chackylee
 * @description:
 * @create: 2022-05-19 10:34
 **/
public class MyGenerateySeq implements Seq {

    private AbstractSeq abstractSeq;

    public void setAbstractSeq(AbstractSeq abstractSeq) {
        this.abstractSeq = abstractSeq;
    }

    @Override
    public long getSeq(String key) {
        return abstractSeq.getSeq(key);
    }
}
