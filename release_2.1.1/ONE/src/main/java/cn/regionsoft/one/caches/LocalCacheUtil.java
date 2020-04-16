package cn.regionsoft.one.caches;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
//import com.google.common.cache.CacheLoader;
//import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

public class LocalCacheUtil {
	public static final String GET_TARGET_CONTEXT = "getTargetContext-";
	public static final String GET_MATCHED_LISTENERS = "getMatchedListeners-";
	public static final String URL_NODEMAP_CACHE = "url-";
	public static final String GET_MANAGED_BEAN = "getManagedBean-";
	public static final String GET_FIELD = "getField-";
	public static final String GET_DECLARED_METHODS = "getDeclaredMethods-";
	
	public static final String GET_FIELDS_AS_STR = "getField-as-str";
	public static final String GET_ALL_FIELDS = "getAllFields";
	
	private static Cache<String, Object> cache = null;
	private static final int EXPIRE_HOURS = 72;
	static {
		cache = CacheBuilder.newBuilder()
				/*设置缓存容器的初始容量大小为100*/  
                .initialCapacity(500)  
                /*设置缓存容器的最大容量大小为1000*/  
                .maximumSize(3000)  
                /*设置记录缓存命中率*/  
                //.recordStats()  
                /*可以同时写缓存的线程数*/  
                .concurrencyLevel(8)
                /*过期时间*/
                .expireAfterAccess(EXPIRE_HOURS, TimeUnit.HOURS)
				//.expireAfterWrite(4, TimeUnit.SECONDS)
				.removalListener(new RemovalListener<String, Object>() {
					@Override
					public void onRemoval(RemovalNotification<String, Object> notification) {
						// TODO Auto-generated method stub
						//System.out.println("remove");
					}

				})
				.build();
				/*.build(new CacheLoader<String, Object>() {
					@Override
					public String load(String key) throws Exception {  
                        return null; 
					}
				});*/
	}
	public static void put(String key,Object val) {
		cache.put(key, val);
	}
	
	public static AtomicInteger a = new AtomicInteger();;
	public static Object get(String key) {
		return cache.getIfPresent(key);
/*		if(result!=null) {
			System.out.println("cache hit "+a.incrementAndGet());
		}*/
		//return result;
		//return cache.getIfPresent(key);
	}
	
	public static void invalidate(String key) {
		cache.invalidate(key);
	}

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		System.out.println(1);
		LocalCacheUtil.put("aa1", "bbb");
		
		System.out.println(LocalCacheUtil.get("aa1"));
	
		System.out.println(LocalCacheUtil.get("aa1"));
		TimeUnit.SECONDS.sleep(2);
		System.out.println(LocalCacheUtil.get("aa1"));
		TimeUnit.SECONDS.sleep(2);
		System.out.println(LocalCacheUtil.get("aa1"));
		TimeUnit.SECONDS.sleep(2);
		System.out.println(LocalCacheUtil.get("aa1"));
		TimeUnit.SECONDS.sleep(5);
		System.out.println(LocalCacheUtil.get("aa1"));
		LocalCacheUtil.invalidate("aa1");
		
	}

}
