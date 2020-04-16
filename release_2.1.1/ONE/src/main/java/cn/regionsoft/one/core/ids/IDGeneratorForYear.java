package cn.regionsoft.one.core.ids;

import java.util.Calendar;
import cn.regionsoft.one.core.CommonUtil;

/**
 * 2个ID生成节点, 可使用到2099年,每秒8192个ID
 * 实现参考 SimpleIDGenerator.java
 * @author fenglj
 */
public class IDGeneratorForYear extends AbstractIDGenerator{
		public IDGeneratorForYear(long workerId){
			//workerIdLength=1  标识支持两个节点
			//secondsLength 25  支持一年索引
			//seqNoLength = 13, 每秒8192个ID
			this(workerId,1,25,13);
		}
		
		public IDGeneratorForYear(long workerId,int workerIdLength,int secondsLength,int seqNoLength){
			super(workerId, workerIdLength, secondsLength, seqNoLength);
		}
		
		public String getNextID() throws Exception{
			try{
				Calendar calendar = Calendar.getInstance();
				int year = calendar.get(Calendar.YEAR);
				calendar.set(year, 0, 1, 0, 0, 0);
				long baseTime = calendar.getTimeInMillis()/1000;//ID基准时间
				
				long result = getNextIdForBaseTime(baseTime);

				return year%2000+CommonUtil.encodeLong(result);
			} catch (Exception e) {
				throw e;
			}
			finally{
				reentrantLock.unlock();
			}
		}
		
		
}
