package cn.regionsoft.one.disruptor;

import com.lmax.disruptor.EventFactory;

public class LongEventFactory implements EventFactory<LongEvent>{
    public LongEvent newInstance(){
        return new LongEvent();
    }
}