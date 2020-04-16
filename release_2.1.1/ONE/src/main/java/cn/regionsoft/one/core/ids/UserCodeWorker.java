package cn.regionsoft.one.core.ids;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import cn.regionsoft.one.common.Code64Util;
import cn.regionsoft.one.core.CommonUtil;

/**
 * @author fenglj
 * 1 byte sign符
 * 1 byte 补齐长度 1
 * 128个ID节点  每个节点每秒最多产生8388608个ID  可使用到2150年终
 */
public class UserCodeWorker {
	//second length 2150年
	private static final int SECOND_LENGTH = 32;
	//worker id length 0 - 7
	private static final int WORKER_ID_LENGTH = 3;
	//seqno length 2^SEQ_LENGTH 支持每秒262144个
	private static final int SEQ_LENGTH = 18;
	
	private static final long MAX_SEQ_PER_SECOND = (long) Math.pow(2,SEQ_LENGTH)-1;
	private static final long MAX_WORKER_ID = (long) Math.pow(2,WORKER_ID_LENGTH)-1;
	private static final long MIN_ID = 0;//补齐长度

	private long workIdVal = 0L;
	private Lock reentrantLock = new ReentrantLock();
	private long secondForRenew = 0L;
	private long calResult = 0L;
	private long seqNo = 0L;
	private final String baseTimeStr = "2018-01-01";
	private long baseTime = 0L;
	private final String datePattern = "yyyy-MM-dd";
	
	public UserCodeWorker(long workerId){
		if(workerId<0||workerId>MAX_WORKER_ID){
			throw new RuntimeException("workerId range : 0- "+MAX_WORKER_ID);
		}
		workIdVal = workerId<<SEQ_LENGTH;
		try {
			SimpleDateFormat sdf = CommonUtil.getSimpleDateFormat(datePattern);
			Date dt = sdf.parse(baseTimeStr);
			baseTime = dt.getTime()/1000;
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	
	public long getNextID() throws Exception{
		try{
			reentrantLock.lock();
			long currentSecond = System.currentTimeMillis()/1000;
			if(secondForRenew!=currentSecond) {
				secondForRenew = currentSecond;
				calResult = MIN_ID+((secondForRenew-baseTime)<<(WORKER_ID_LENGTH+SEQ_LENGTH)) + workIdVal;
				seqNo = 0L;
			}
			else {
				seqNo++;
				if(seqNo>MAX_SEQ_PER_SECOND) {
					 throw new Exception("IDWorker is exhausted for this second");
				}
			}
			long result = calResult + seqNo;
			return result;
		} catch (Exception e) {
			throw e;
		}
		finally{
			reentrantLock.unlock();
		}
	}

	public static void main(String[] args) throws Exception {
		Calendar cal = Calendar.getInstance();
		UserCodeWorker idWorkerForBiz = new UserCodeWorker(1);
		System.out.println(idWorkerForBiz.getNextID());
		System.out.println(Code64Util.longToStr64(idWorkerForBiz.getNextID()));
		if(true)return;
		Long time  = System.currentTimeMillis();
		int i = 0;
		try {
			
			for(; i <100000000 ; i ++) {
				idWorkerForBiz.getNextID();
			}
			
		}
		finally {
			System.out.println(i);
			System.out.println(System.currentTimeMillis() - time);
		}
	

		
//		System.out.println((long) Math.pow(2,20));
//		long second =  (long) Math.pow(2, 32);
//		Calendar cal = Calendar.getInstance();
//		cal.setTimeInMillis(second*1000);
//		System.out.println(cal.getTime());
	}
}
//4292373583593472
   

