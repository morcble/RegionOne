package cn.regionsoft.one.utils;

public class ThreadHolder {
	private static ThreadHolder instance = new ThreadHolder();
	private ThreadLocal<ThreadData> threadDatas = new ThreadLocal<ThreadData>();
	private ThreadHolder(){}
	public static ThreadHolder getInstance(){
		return instance;
	}
	public ThreadLocal<ThreadData> getThreadDatas() {
		return threadDatas;
	}
	public void setThreadDatas(ThreadLocal<ThreadData> threadDatas) {
		this.threadDatas = threadDatas;
	}
}
