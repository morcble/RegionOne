package cn.regionsoft.one.core.ids;

import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.core.SystemContext;
import cn.regionsoft.one.properties.ConfigUtil;

public class IDGenerator {
	private static int CLUSTER_ID = 0;//0-9

	private static SnowflakeIDWorker snowflakeIDWorker = new SnowflakeIDWorker(CLUSTER_ID);
	public static Long generateSnowflakeID(){
		return snowflakeIDWorker.nextId();
	}

//	private static IDWorker idWorker = null;
//	static {
//		String clusterId = ConfigUtil.getProperty("clusterId");
//		if(clusterId==null)clusterId="1";
//		Long tmp = Long.valueOf(clusterId);
//		idWorker = new IDWorker(tmp,Constants.SYSTEM_ROOT+Constants.SYSTEM_SEPERATOR+"IDS",100000);
//	}

	
	public static String getStringID() throws Exception{
		return String.valueOf(getLongID());
	}
	
	public static Long getLongID() throws Exception{
		//return idWorker.getNextID();
		return CloudIDGenerator.getLongID();
	}
	
	public static void main(String[] args){
		System.out.println(Long.MAX_VALUE);
	}
}



