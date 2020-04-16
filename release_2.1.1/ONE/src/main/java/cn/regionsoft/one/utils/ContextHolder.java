package cn.regionsoft.one.utils;

import cn.regionsoft.one.core.H2OContext;

public class ContextHolder {
	private static ContextHolder instance = new ContextHolder();
	private ThreadLocal<H2OContext> threadDatas = new ThreadLocal<H2OContext>();
	private ContextHolder(){}
	public static ContextHolder getInstance(){
		return instance;
	}
	public H2OContext getCurrentContext() {
		return threadDatas.get();
	}
	public void setCurrentContext(H2OContext systemContext) {
		threadDatas.set(systemContext);
	}
}
