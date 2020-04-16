package cn.regionsoft.one.assist.aop.interfaces;


public interface Interceptor {
	void beforeInvoke();
	
	Object invoke(Class withinClass,String methodName,Class<?>[] parameterTypes, Object[] parameters);
	
	void afterInvoke();
}
