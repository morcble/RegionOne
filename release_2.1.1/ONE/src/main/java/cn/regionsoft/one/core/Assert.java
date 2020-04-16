package cn.regionsoft.one.core;

public class Assert {
	public static void notNull(Object target,String msg) {
		if(target==null) {
			throw new RuntimeException(msg);
		}
	}
	
	public static void notEmpty(String target,String msg) {
		if(CommonUtil.isEmpty(target)) {
			throw new RuntimeException(msg);
		}
	}
}
