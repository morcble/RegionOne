package cn.regionsoft.one.utils;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.core.dbconnection.H2OConnection;

public class ThreadData {
	/**
	 * ContectName - ThreadContextData
	 */
	private HashMap<String,ThreadContextData> threadContextDataMap = new HashMap<String,ThreadContextData>();
	
	
	private SimpleDateFormat simpleDateFormat = null;
	private String previousLocale = null;

	public SimpleDateFormat getSimpleDateFormat(String pattern, String locale) {
		if(simpleDateFormat==null||previousLocale!=locale) {
			if("en".equalsIgnoreCase(locale)) {
				simpleDateFormat = new SimpleDateFormat(pattern,Locale.ENGLISH);
				simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
				previousLocale = "en";
			}
			else {
				simpleDateFormat = new SimpleDateFormat(pattern,Locale.SIMPLIFIED_CHINESE);
				simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
				previousLocale = "cn";
			}
		}
		else {
			simpleDateFormat.applyPattern(pattern);
		}
		return simpleDateFormat;
	}
	
	public SimpleDateFormat getSimpleDateFormat(String pattern) {
		if(simpleDateFormat==null) {
			simpleDateFormat = new SimpleDateFormat(pattern,Locale.ENGLISH);
			simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		}
		else {
			simpleDateFormat.applyPattern(pattern);
		}
		return simpleDateFormat;
	}
	
	public int getTransactionDepth(H2OContext context) {
		return getThreadContextData(context).getTransactionDepth();
	}
	public void setTransactionDepth(int transactionDepth,H2OContext context) {
		//System.out.println("transactionDepth="+transactionDepth);
		getThreadContextData(context).setTransactionDepth(transactionDepth);
	}
	
	public int getNonTransactionDepth(H2OContext context) {
		return getThreadContextData(context).getNonTransactionDepth();
	}
	public void setNonTransactionDepth(int nonTransactionDepth,H2OContext context) {
		//System.out.println("nonTransactionDepth="+nonTransactionDepth);
		 getThreadContextData(context).setNonTransactionDepth(nonTransactionDepth);
	}

	public H2OConnection getTransactionConnection(H2OContext context) {
		return getThreadContextData(context).getTransactionConnection();
	}
	public void setTransactionConnection(H2OConnection transactionConnection,H2OContext context) {
		getThreadContextData(context).setTransactionConnection(transactionConnection);
	}
	
	public H2OConnection getNoTrxConnection(H2OContext context) {
		return getThreadContextData(context).getNoTrxConnection();
	}
	public void setNoTrxConnection(H2OConnection noTrxConnection,H2OContext context) {
		getThreadContextData(context).setNoTrxConnection(noTrxConnection);
	}

	
	ThreadContextData getThreadContextData(H2OContext context){
		String contextName = context.getContextName();
		ThreadContextData threadContextData = threadContextDataMap.get(contextName);
		if(threadContextData==null){
			threadContextData = new ThreadContextData();
			threadContextDataMap.put(contextName, threadContextData);
		}
		return threadContextData;
	}
	
	
	
}
