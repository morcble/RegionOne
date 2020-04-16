package cn.regionsoft.one.bigdata.impl.hbase;


public class HbaseThreadHolder {
	private static HbaseThreadHolder instance = new HbaseThreadHolder();
	private ThreadLocal<HbaseDBConnection> threadLocal = new ThreadLocal<HbaseDBConnection>();
	private HbaseThreadHolder(){}
	public static HbaseThreadHolder getInstance(){
		return instance;
	}
	public HbaseDBConnection getThreadDatas() {
		return threadLocal.get();
	}
	public void setThreadDatas(HbaseDBConnection hbaseDBConnection) {
		threadLocal.set(hbaseDBConnection);
	}
}
