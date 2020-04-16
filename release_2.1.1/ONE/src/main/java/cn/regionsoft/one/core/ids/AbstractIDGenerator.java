package cn.regionsoft.one.core.ids;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author fenglj
 * 1 byte sign符
 * 1 byte 补齐长度 1
 * 7 byte 128个ID节点  一共可以定义128个生成节点
 * 32 byte seconds   从baseTimeStr开始可使用136年
 * 23 byte seqno per second   每秒最多产生8388608个ID
 */
public abstract class AbstractIDGenerator {
	protected int workerIdLength = 0;
	protected int seqNoLength = 0;

	protected long maxSeqPerSecond = 0;
	protected long maxWorkerId = 0;
	protected long minId = 0;

	protected long workIdVal = 0L;
	protected Lock reentrantLock = new ReentrantLock();
	protected long secondForRenew = 0L;
	protected long calResult = 0L;
	protected long seqNo = 0L;
	
	public AbstractIDGenerator(long workerId,int workerIdLength,int secondsLength,int seqNoLength) {
		this.workerIdLength = workerIdLength;
		this.seqNoLength = seqNoLength;

		maxSeqPerSecond = (long) Math.pow(2,seqNoLength)-1;
		maxWorkerId = (long) Math.pow(2,workerIdLength)-1;
		minId = (long) Math.pow(2,secondsLength+workerIdLength+seqNoLength);//补齐长度
		
		
		if(workerId<0||workerId>maxWorkerId){
			throw new RuntimeException("workerId range : 0- "+maxWorkerId);
		}
		workIdVal = workerId<<seqNoLength;
	}
	
	protected long getNextIdForBaseTime(long baseTime) throws InterruptedException {
		reentrantLock.lock();
		long currentSecond = System.currentTimeMillis()/1000;
		if(secondForRenew!=currentSecond) {
			secondForRenew = currentSecond;
			calResult = minId+((secondForRenew-baseTime)<<(workerIdLength+seqNoLength)) + workIdVal;
			seqNo = 0L;
		}
		else {
			seqNo++;
			if(seqNo>maxSeqPerSecond) {
				 Thread.sleep(50);
				 return getNextIdForBaseTime(baseTime);
			}
		}
		long result = calResult + seqNo;
		return result;
	}
}
