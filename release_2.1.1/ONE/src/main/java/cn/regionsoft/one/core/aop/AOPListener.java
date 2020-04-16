package cn.regionsoft.one.core.aop;

import java.lang.reflect.Method;
import cn.regionsoft.one.core.H2OContext;

public interface AOPListener {
	/**
	 * 
	 * @param obj
	 * @param method
	 * @param args
	 * @param context
	 * @return  是否继续执行被拦截的方法,返回false标识不继续执行被拦截的主方法
	 */
	public Object beforeInvoke(Object obj, Method method, Object[] args, H2OContext context);
	
	public Object afterInvoke(Object obj, Method method, Object[] args,  Object result,H2OContext context);

	public void exceptionInvoke(Throwable e,H2OContext context);

	public void finalInvoke(Object obj, Method method, Object[] args,  Object result,H2OContext context);
}
