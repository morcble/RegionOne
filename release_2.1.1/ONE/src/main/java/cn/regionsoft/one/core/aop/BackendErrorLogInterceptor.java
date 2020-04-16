package cn.regionsoft.one.core.aop;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import cn.regionsoft.one.core.CommonUtil;

public interface BackendErrorLogInterceptor {
	/**
	 * if return false, will skip to log in log file
	 */
	boolean debug(Object... objs);
	/**
	 * if return false, will skip to log in log file
	 */
	boolean error(Object... objs);
	/**
	 * if return false, will skip to log in log file
	 */
	boolean info(Object... objs);
	
	/*
	*/
	

}
