package cn.regionsoft.one.core;

public class LogDepthManager {
	private static LogDepthManager instance = new LogDepthManager();
	private ThreadLocal<Integer> cache = new ThreadLocal<Integer>();
	private LogDepthManager(){}
	
	public static LogDepthManager getInstance(){
		return instance;
	}
	
	static{
		if(instance==null){
			instance = new LogDepthManager();
		}
	}
	
	public void enterLog(){
		Integer depth = cache.get();
		if(depth==null){
			depth = 1;	
		}
		else{
			depth++;
		}
		cache.set(depth);
	}
	
	public void exitLog(){
		Integer depth = cache.get();
		depth--;
		if(depth<=0){
			cache.remove();
		}
		else{
			cache.set(depth);
		}
	}
	
	public Integer getLogDepth(){
		return cache.get();
	}
	
	
}
