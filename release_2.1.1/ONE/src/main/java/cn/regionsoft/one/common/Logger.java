package cn.regionsoft.one.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.LogDepthManager;
import cn.regionsoft.one.core.RequestInfoHolder;
import cn.regionsoft.one.core.SystemContext;
import cn.regionsoft.one.core.aop.BackendErrorLogInterceptor;
import cn.regionsoft.one.core.auth.dto.RequestInfoDto;
import cn.regionsoft.one.core.exception.BizException;
import cn.regionsoft.one.core.logger.CustomizedClassloaderUtil;
import cn.regionsoft.one.properties.ConfigUtil;

public class Logger{
	public static final int OPTYPE_C = 0; //to create record
	public static final int OPTYPE_R = 1; //to retrieve record
	public static final int OPTYPE_U = 2; //to update record
	public static final int OPTYPE_D = 3; //to delete record
	
	public static final int OPTYPE_C_ED = 4; //created record
	public static final int OPTYPE_U_ED = 5; //updated record
	public static final int OPTYPE_D_ED = 6; //deleted record
	
	public static final int OPTYPE_R_CACHE = 7; //retrieved record from cache
	public static final int OPTYPE_R_DB = 8; //retrieved record from db
	
	public static final int OPTYPE_RAMOUNT = 13;//to retrieve amount
	public static final int OPTYPE_RAMOUNT_CACHE_ED = 14; //retrieved amount from cache
	public static final int OPTYPE_RAMOUNT_DB_ED = 15; //retrieved amount from db
	
	public static final int OPTYPE_RLS= 16; //to retrieve list
	public static final int OPTYPE_RLS_CACHE_ED = 17; //retrieved list from cache
	public static final int OPTYPE_RLS_DB_ED = 18; //retrieved list from db
	
	
	
	//logger switch
	public static boolean specialLogEnabled = true;
	public static Logger getLogger(Class<?> clazz){
		return new Logger(clazz);
	}
	
	private Object logInstance;
	
	private static LogDepthManager logDepthManager = LogDepthManager.getInstance();


	private Logger(Class<?> clazz) {
		try {
			String loggerMode = ConfigUtil.getProperty("logger.proxy");
			
			if(Boolean.valueOf(loggerMode)) {
				logInstance = CustomizedClassloaderUtil.invokeMethod("getLogger",clazz);
			}
			else {
				logInstance = org.apache.logging.log4j.LogManager.getLogger(clazz);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void debug(Object... objs){
		try{
			logDepthManager.enterLog();
			
			if(logDepthManager.getLogDepth()>3)return;
			
			BackendErrorLogInterceptor in = SystemContext.getInstance().getBackendErrorLogInterceptor();
			boolean logToFile = true;
			if(in!=null){
				logToFile = in.debug(objs);
			}
			if(!logToFile)return;
			
			StringBuilder buf = new StringBuilder();
			buf.append(lineNumber());
			for (Object obj : objs) {
				if(obj instanceof Throwable){
					buf.append(constructStackTrace((Throwable) obj));
				}
				else if(obj instanceof Object[]){
					buf.append("[");
					Object[] array = (Object[])obj;
					for(int i = 0 ; i <array.length ; i++){
						buf.append(array[i]);
						if(i!=(array.length-1))buf.append(",");
					}
					buf.append("]");
				}
				else{
					buf.append(obj);
				}
			}
			invokeLogManager(logInstance,"debug",buf.toString());
		}
		finally{
			logDepthManager.exitLog();
		}
		
	}
	

	public void error(Object... objs){
		try{
			logDepthManager.enterLog();
			
			if(logDepthManager.getLogDepth()>3)return;
			
			BackendErrorLogInterceptor in = SystemContext.getInstance().getBackendErrorLogInterceptor();
			boolean logToFile = true;
			if(in!=null){
				logToFile = in.error(objs);
			}
			if(!logToFile)return;
			
			StringBuilder buf = new StringBuilder();
			buf.append(lineNumber());
			boolean bizException = false;
			for (Object obj : objs) {
				if(obj instanceof Throwable){
					if(obj instanceof BizException){
						bizException = true;
					}
					buf.append(constructStackTrace((Throwable) obj));
				}
				else if(obj instanceof Object[]){
					buf.append("[");
					Object[] array = (Object[])obj;
					for(int i = 0 ; i <array.length ; i++){
						buf.append(array[i]);
						if(i!=(array.length-1))buf.append(",");
					}
					buf.append("]");
				}
				else{
					buf.append(obj);
				}
				
			}
			if(bizException){
				invokeLogManager(logInstance,"warn",buf.toString());
			}
			else{
				invokeLogManager(logInstance,"error",buf.toString());
			}	
		}
		finally{
			logDepthManager.exitLog();
		}
		
	}
	
	public void info(Object... objs){
		try{
			logDepthManager.enterLog();
			
			if(logDepthManager.getLogDepth()>3)return;
			
			BackendErrorLogInterceptor in = SystemContext.getInstance().getBackendErrorLogInterceptor();
			boolean logToFile = true;
			if(in!=null){
				logToFile = in.info(objs);
			}
			if(!logToFile)return;
			
			StringBuilder buf = new StringBuilder();
			buf.append(lineNumber());
			for (Object obj : objs) {
				if(obj instanceof Throwable){
					buf.append(constructStackTrace((Throwable) obj));
				}
				else if(obj instanceof Object[]){
					buf.append("[");
					Object[] array = (Object[])obj;
					for(int i = 0 ; i <array.length ; i++){
						buf.append(array[i]);
						if(i!=(array.length-1))buf.append(",");
					}
					buf.append("]");
				}
				else{
					buf.append(obj);
				}
			}
			invokeLogManager(logInstance,"info",buf.toString());
		}
		finally{
			logDepthManager.exitLog();
		}	
	}
	
	
	
	private String constructStackTrace(Throwable t) {
		try {
			if(t instanceof BizException){
				return (t.getMessage());
			}
			else{
				return getStackTrace(t);
			}
		} catch (Exception e) {
			invokeLogManager(logInstance,"error",e.getMessage());
			return e.getMessage();
		}

	}
	
	public static String getStackTrace(Throwable aThrowable) throws RuntimeException {
		Writer stack = null;
		PrintWriter printWriter = null;
		try{
			stack = new StringWriter();
			printWriter = new PrintWriter(stack);
			aThrowable.printStackTrace(printWriter);
			return stack.toString();
		}
		catch(Exception e){
			throw new RuntimeException(e);
			
		}
		finally{
			CommonUtil.closeQuietly(printWriter);
			CommonUtil.closeQuietly(stack);
		}
	}

	private String lineNumber() {
		RequestInfoDto requestInfoDto = RequestInfoHolder.getInstance().getRequestInfo();
		String reqId = null;
		if(requestInfoDto!=null) {
			reqId = requestInfoDto.getRequestId();
		}
		
		StackTraceElement threadTrackArray[] = Thread.currentThread().getStackTrace();
		if (threadTrackArray.length > 3) {
			return ":" + Integer.toString(threadTrackArray[3].getLineNumber()) + "-" +" [requestId:"+reqId+"] ";
		}
		return " [requestId:"+reqId+"] ";
	}
	
	public static void invokeLogManager(Object logManager,String methodName, Object... args){
		Method[] methods = logManager.getClass().getMethods();
		st:for(Method method:methods){
			if(method.getName().equals(methodName)){
				if(args==null){
					args = new Object[0];
				}
				
				if(method.getParameterCount() == args.length){
					Class<?>[] paraTypes = method.getParameterTypes();
					for(int i = 0 ; i <paraTypes.length ; i++){
						if(paraTypes[i] != args[i].getClass()){
							continue st;
						}
					}
					
					try {
						method.invoke(logManager, args);
						return;
					} catch (Exception e) {
						e.printStackTrace();
						return;
					} 
				}
			}
		}
	}

	public void warn(Exception e) {
		invokeLogManager(logInstance,"warn",e.getMessage());	
	}
	
	public void warn(String msg) {
		invokeLogManager(logInstance,"warn",msg);	
	}
}
