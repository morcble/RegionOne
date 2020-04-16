package cn.regionsoft.one.assist.aop.interfaces.test;


import cn.regionsoft.one.assist.aop.interfaces.Interceptor;

public class SampleInterceptorImpl implements Interceptor{

	@Override
	public void beforeInvoke() {
		//System.out.println("beforeInvoke");
		
	}

	@Override
	public Object invoke(Class withinClass,String methodName,Class<?>[] parameterTypes, Object[] parameters) {
		// TODO Auto-generated method stub
		//System.out.println("invoke");
		return "aa";
	}

	@Override
	public void afterInvoke() {
		//System.out.println("afterInvoke");
		
	}

}
