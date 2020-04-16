package cn.regionsoft.one.core.ids;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import cn.regionsoft.one.core.CommonUtil;

/**
 * @author fenglj
 * 1 byte sign符
 * 1 byte 补齐长度 1
 * 实际占用字节数 7+32+23
 */
public class SimpleIDGenerator extends AbstractIDGenerator{
		private final String baseTimeStr = "2019-10-01";//生成器基准开始时间
		private long baseTime = 0L;
		private final String datePattern = "yyyy-MM-dd";
		
		/**
		 * @author fenglj
		 * 1 byte sign符
		 * 1 byte 补齐长度 1
		 * 7 byte 128个ID节点  一共可以定义128个生成节点
		 * 32 byte seconds   从baseTimeStr开始可使用136年
		 * 23 byte seqno per second   每秒最多产生8388608个ID
		 */
		public SimpleIDGenerator(long workerId){
			this(workerId,7,32,23);
		}
		
		public SimpleIDGenerator(long workerId,int workerIdLength,int secondsLength,int seqNoLength){
			super(workerId, workerIdLength, secondsLength, seqNoLength);
			
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
				return getNextIdForBaseTime(baseTime);
			} catch (Exception e) {
				throw e;
			}
			finally{
				reentrantLock.unlock();
			}
		}
}
