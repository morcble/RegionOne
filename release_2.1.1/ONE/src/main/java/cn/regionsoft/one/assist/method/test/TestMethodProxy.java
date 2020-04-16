package cn.regionsoft.one.assist.method.test;

import cn.regionsoft.one.assist.method.MethodProxyWithValueReturn;
import cn.regionsoft.one.assist.method.MethodProxyAssist;
import cn.regionsoft.one.assist.method.MethodProxyNoValueReturn;

public class TestMethodProxy {

	public static void main(String[] args) throws Exception {
		Object c = new MethodSample();
		long count = 1000000;
		/*c.setReturn_code("c");
		c.setReturn_msg("m");*/
		//test(c, count);
		String methodName = "test";
		Object[] parameters = new Object[] { "hello"};
		Class<?>[] parameterTypes = new Class[] {String.class};
		long time = System.currentTimeMillis();
		MethodProxyWithValueReturn proxy = (MethodProxyWithValueReturn) MethodProxyAssist.genMethodProxy(c,"test",parameterTypes,true);
		//System.out.println(System.currentTimeMillis()-time);
		
		time = System.currentTimeMillis();
		for(int i = 0 ; i <2000000;i++) {
			c.getClass().getMethod(methodName, parameterTypes).invoke(c, parameters);
		}
		System.out.println(System.currentTimeMillis()-time);

		time = System.currentTimeMillis();
		for(int i = 0 ; i <2000000;i++) {
			proxy.excute(c, parameters);
		}
		System.out.println(System.currentTimeMillis()-time);
		
		
		MethodProxyNoValueReturn proxyNoVal = (MethodProxyNoValueReturn) MethodProxyAssist.genMethodProxy(c,"noValueTest",null,false);
		time = System.currentTimeMillis();
		for(int i = 0 ; i <2000000;i++) {
			proxyNoVal.excute(c, null);
		}
		System.out.println(System.currentTimeMillis()-time);

	}

}
