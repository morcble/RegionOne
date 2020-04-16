package cn.regionsoft.one.core.ids;

import cn.regionsoft.one.properties.ConfigUtil;

public class CloudIDGenerator {
	private static SimpleIDGenerator idWorkerForCloud = null;
	static {
		String clusterId = ConfigUtil.getProperty("clusterId");
		if(clusterId==null)clusterId="0";
		Long tmp = Long.valueOf(clusterId);
		/*
		 * *7 byte 128个ID节点  一共可以定义128个生成节点
		 * 32 byte seconds   从baseTimeStr开始可使用136年
		 * 23 byte seqno per second   每秒最多产生8388608个ID
		 */
		idWorkerForCloud = new SimpleIDGenerator(tmp);
	}
	
	public static String getStringID() throws Exception{
		return String.valueOf(idWorkerForCloud.getNextID());
	}
	
	public static Long getLongID() throws Exception{
		return idWorkerForCloud.getNextID();
	}
}
