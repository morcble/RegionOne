package cn.regionsoft.one.test;

import cn.regionsoft.one.core.CommonUtil;

public class Promise {
	public static <T> T notNull(T o, String errorMsg) {
		if(o==null) throw new RuntimeException(errorMsg);
		return o;
	}

	public static <T> T  notEmpty(T o, String errorMsg) {
		if(CommonUtil.isEmpty(o)) throw new RuntimeException(errorMsg);
		return o;
	}
}
