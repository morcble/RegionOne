package cn.regionsoft.one.core.ids;

import cn.regionsoft.one.common.Code64Util;
import cn.regionsoft.one.properties.ConfigUtil;

/**
 * 每秒20万个
 * @author fenglj
 *
 */
public class UserCodeGenerator {
	private static UserCodeWorker userCodeWorker = null;
	static {
		String clusterId = ConfigUtil.getProperty("clusterId");
		if(clusterId==null)clusterId="0";
		Long tmp = Long.valueOf(clusterId);
		userCodeWorker = new UserCodeWorker(tmp);
	}
	
	public static String getStringID() throws Exception{
		return Code64Util.longToStr64(userCodeWorker.getNextID());
	}
	public static void main(String[] args) throws Exception {
		for(int i = 0 ; i <10 ; i++)
		System.out.println(UserCodeGenerator.getStringID());
	}
}
