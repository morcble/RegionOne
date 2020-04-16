package cn.regionsoft.one.assist.aop.interfaces.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import cn.regionsoft.one.assist.aop.interfaces.Interceptor;
import cn.regionsoft.one.assist.aop.interfaces.InterfaceProxyAssist;

import javassist.CannotCompileException;
import javassist.NotFoundException;

public class AOPTest {

	public static void main(String[] args) throws Exception {
		AopTestInterFace aopTestInterface1 = (AopTestInterFace) InterfaceProxyAssist.proxyInterface(AopTestInterFace.class,new Interceptor() {
			@Override
			public void beforeInvoke() {
				// TODO Auto-generated method stub
			}

			@Override
			public Object invoke(Class withinClass, String methodName, Class<?>[] parameterTypes, Object[] parameters) {
				return "aa";
			}

			@Override
			public void afterInvoke() {
				// TODO Auto-generated method stub	
			}
			
		});
		
		AopTestInterFace aopTestInterface2 = (AopTestInterFace) Proxy.newProxyInstance(AopTestInterFace.class.getClassLoader(),
				new Class<?>[] { AopTestInterFace.class }, new InvocationHandler() {
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) {
						return "aa";
					}
				});
		

		long round = 2000000000;
		long time = 0;
		
		time = System.currentTimeMillis();
		for(int i = 0 ; i <round;i++) {
			aopTestInterface2.hello();
		}
		System.out.println(System.currentTimeMillis()-time);
		
		time = System.currentTimeMillis();
		for(int i = 0 ; i <round;i++) {
			aopTestInterface1.hello();
		}
		System.out.println(System.currentTimeMillis()-time);
		
		
		
	}

}
