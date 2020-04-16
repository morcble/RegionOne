package cn.regionsoft.one.utils;

import java.util.HashMap;

import cn.regionsoft.one.core.dbconnection.H2OConnection;
import cn.regionsoft.one.core.dbconnection.SQLConnection;

/**
 * 保存带事务的connection 和不带事务的connection
 * 如果transactionDepth>0则使用带事务的connection,transactionDepth=0的时候使用不带事务的connection
 * @author fenglj
 *
 */
public class ThreadContextData {
	private H2OConnection noTrxConnection;
	
	private int nonTransactionDepth = 0;
	
	/*
	 * transactionConnection within tag transactional 
	 */
	private H2OConnection transactionConnection;
	
	private int transactionDepth = 0;
	
	public int getTransactionDepth() {
		return transactionDepth;
	}
	public void setTransactionDepth(int transactionDepth) {
		this.transactionDepth = transactionDepth;
	}
	
	public int getNonTransactionDepth() {
		return nonTransactionDepth;
	}
	public void setNonTransactionDepth(int nonTransactionDepth) {
		this.nonTransactionDepth = nonTransactionDepth;
	}

	public H2OConnection getTransactionConnection() {
		return transactionConnection;
	}
	public void setTransactionConnection(H2OConnection transactionConnection) {
		this.transactionConnection = transactionConnection;
	}
	public H2OConnection getNoTrxConnection() {
		return noTrxConnection;
	}
	public void setNoTrxConnection(H2OConnection noTrxConnection) {
		this.noTrxConnection = noTrxConnection;
	}




	/**
	 * 事务内的数据缓存
	 */
	private HashMap<String,Object> transactionCache =  new HashMap<String,Object>();
	
	public Object getTransactionCache(String key){
		return transactionCache.get(key);
	}
	
	public void addTransactionCache(String key,Object value){
		transactionCache.put(key, value);
	}
	
	public void removeTransactionCacheByKey(String key){
		transactionCache.remove(key);
	}
	
	public void clearTransactionCache(){
		transactionCache.clear();
	}
	public boolean transactionCacheConstainKey(String key) {
		return transactionCache.containsKey(key);
	}
}
