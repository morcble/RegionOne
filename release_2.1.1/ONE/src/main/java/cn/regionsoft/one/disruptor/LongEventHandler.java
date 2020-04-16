package cn.regionsoft.one.disruptor;

import com.lmax.disruptor.EventHandler;

public class LongEventHandler implements EventHandler<LongEvent>{
	int count=0;
	long starttime=0L;
    public void onEvent(LongEvent event, long sequence, boolean endOfBatch){
    	
    	count++;
    	System.out.println(Thread.currentThread());
    }
}