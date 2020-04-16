package cn.regionsoft.one.caches;


public class CacheThreadHolder {
	private static CacheThreadHolder instance = new CacheThreadHolder();
	private ThreadLocal<CacheThreadData> cache = new ThreadLocal<CacheThreadData>();
	private CacheThreadHolder(){}
	
	public static CacheThreadHolder getInstance(){
		return instance;
	}
	
	static{
		if(instance==null){
			instance = new CacheThreadHolder();
		}
	}
	
	public CacheThreadData getCacheThreadData(){
		CacheThreadData result = cache.get();
		if(result==null) {
			result = new CacheThreadData();
			cache.set(result);
		}
		
		return result;
	}
	
	public void setRequestInfo(CacheThreadData cacheThreadData){
		cache.set(cacheThreadData);
	}
}
