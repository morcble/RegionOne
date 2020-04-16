package cn.regionsoft.one.utils;

import cn.regionsoft.one.core.H2OContext;

public class TransactionUtil {
	public static boolean isInTransaction(H2OContext h2oContext){
		ThreadHolder holder = ThreadHolder.getInstance();
		ThreadData threadData = holder.getThreadDatas().get();
		if(threadData==null) {
			threadData = new ThreadData();
			holder.getThreadDatas().set(threadData);
		}
		if(threadData.getTransactionDepth(h2oContext)==0) return false;
		else return true;			
	}
	
	/**
	 * 事务内的数据缓存
	 * @param h2oContext
	 * @param key
	 * @return
	 */
	public static boolean transactionCacheConstainKey(H2OContext h2oContext,String key){
		ThreadHolder holder = ThreadHolder.getInstance();
		ThreadData threadData = holder.getThreadDatas().get();
		ThreadContextData threadContextData = threadData.getThreadContextData(h2oContext);
		return threadContextData.transactionCacheConstainKey(key);
	}
	
	public static Object getTransactionCache(H2OContext h2oContext,String key){
		ThreadHolder holder = ThreadHolder.getInstance();
		ThreadData threadData = holder.getThreadDatas().get();
		ThreadContextData threadContextData = threadData.getThreadContextData(h2oContext);
		return threadContextData.getTransactionCache(key);
	}
	
	public static void addTransactionCache(H2OContext h2oContext,String key,Object value){
		ThreadHolder holder = ThreadHolder.getInstance();
		ThreadData threadData = holder.getThreadDatas().get();
		ThreadContextData threadContextData = threadData.getThreadContextData(h2oContext);
		threadContextData.addTransactionCache(key, value);
	}
	
	public static void removeTransactionCacheByKey(H2OContext h2oContext,String key){
		ThreadHolder holder = ThreadHolder.getInstance();
		ThreadData threadData = holder.getThreadDatas().get();
		ThreadContextData threadContextData = threadData.getThreadContextData(h2oContext);
		threadContextData.removeTransactionCacheByKey(key);
	}
	
	public static void clearTransactionCache(H2OContext h2oContext){
		ThreadHolder holder = ThreadHolder.getInstance();
		ThreadData threadData = holder.getThreadDatas().get();
		ThreadContextData threadContextData = threadData.getThreadContextData(h2oContext);
		threadContextData.clearTransactionCache();
	}
}
