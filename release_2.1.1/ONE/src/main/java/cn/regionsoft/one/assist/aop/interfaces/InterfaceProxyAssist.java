package cn.regionsoft.one.assist.aop.interfaces;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.regionsoft.one.assist.method.MethodProxyWithValueReturn;
import cn.regionsoft.one.assist.method.MethodProxyAssist;
import cn.regionsoft.one.assist.method.MethodProxyNoValueReturn;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

public class InterfaceProxyAssist {
	/*
	 * intercface class interceptor class, generated object
	 */
	private static Map<Integer,Object> interfaceProxyClassMap = new ConcurrentHashMap<Integer,Object>();
	
	public static Object proxyInterface(Class<?> interfaceClass, Class<? extends Interceptor> interceptorImplClass) throws InstantiationException, IllegalAccessException, CannotCompileException, NotFoundException, IOException {
		Interceptor interceptor = interceptorImplClass.newInstance();
		return proxyInterface(interfaceClass,interceptor);
	}
	
	/*
	 * 根据interface创建一个实例，用于执行interceptor中的方法
	 * @param interfaceClass
	 * @param interceptorImplClass
	 * @return
	 * @throws NotFoundException 
	 * @throws CannotCompileException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws IOException 
	 */
	public static Object proxyInterface(Class<?> interfaceClass, Interceptor interceptor) throws InstantiationException, IllegalAccessException, CannotCompileException, NotFoundException, IOException {
		int hashCode = interfaceClass.getName().hashCode()*31+interceptor.getClass().getName().hashCode();
		boolean flag = false;
		if(hashCode<0) {
			hashCode=-hashCode;
			flag = true;
		}
		Object genObject = interfaceProxyClassMap.get(hashCode);
		if(genObject!=null) {
			return genObject;
		}
		synchronized(InterfaceProxyAssist.class){
			genObject = interfaceProxyClassMap.get(hashCode);
			if(genObject!=null) return genObject;
			
			ClassPool pool = ClassPool.getDefault();
	        CtClass cc = null;
	        if(flag) {
	        	cc = pool.makeClass(interfaceClass.getName()+"Impl__"+hashCode);
	        }
	        else {
	        	cc = pool.makeClass(interfaceClass.getName()+"Impl_"+hashCode);
	        }
	        
	        cc.addInterface(pool.get(interfaceClass.getName()));
			
	        StringBuilder methodBody = null;
	        Class<?>[] parameterTypes = null;
	        Class<?> returnType = null;
	        String methodName = null;
	
	        //add setter for interface
	        CtField f1 = CtField.make("private "+Interceptor.class.getName()+" interceptor;", cc);
	        cc.addField(f1);
	        
	        methodBody = new StringBuilder("public void setInterceptor("+Interceptor.class.getName()+" interceptor){this.interceptor = interceptor;}");
	        CtMethod m1=CtMethod.make(methodBody.toString(), cc);
	        cc.addMethod(m1);
	        
			Method[] methods = interfaceClass.getDeclaredMethods();
			for(Method method:methods) {
				parameterTypes = method.getParameterTypes();
				returnType = method.getReturnType();
				methodName = method.getName();
				
				methodBody = new StringBuilder("public ");
				methodBody.append(returnType.getName());
				methodBody.append(" ");
				methodBody.append(methodName);
				methodBody.append("(");
				if(parameterTypes!=null) {
					int length = parameterTypes.length;
		        	for(int i = 0 ; i <length ; i++) {
		        		methodBody.append(parameterTypes[i].getName());
		        		methodBody.append(" ");
		        		methodBody.append("arg");
		        		methodBody.append(i);
		        		if(i!=(length-1)) {
		        			methodBody.append(",");
		        		}
		        	}
				}
				methodBody.append("){");
				
				methodBody.append("this.interceptor.beforeInvoke();");
				
				
				StringBuilder parasSb = null;
				StringBuilder parameterTypesSb = null;
				if(parameterTypes!=null&&parameterTypes.length>0) {
					parameterTypesSb = new StringBuilder();
					parasSb = new StringBuilder();
					int length = parameterTypes.length;
		        	for(int i = 0 ; i <length ; i++) {
		        		parasSb.append("arg");
		        		parasSb.append(i);
		        		
		        		parameterTypesSb.append(parameterTypes[i].getName());
		        		parameterTypesSb.append(".class");
		        		if(i!=(length-1)) {
		        			parasSb.append(",");
		        			parameterTypesSb.append(",");
		        		}
		        	}
		        	methodBody.append("Object[] paras = new Object[]{"+parasSb.toString()+"};");
					methodBody.append("Class[] parameterTypes = new Class[]{"+parameterTypesSb.toString()+"};");
				}
				else {
					methodBody.append("Class[] parameterTypes = null;");
					methodBody.append("Object[] paras = null;");
				}
				
				methodBody.append("Class targetInterface = "+interfaceClass.getName()+".class;");
				
				
				if(returnType!=Void.TYPE) {
					methodBody.append("Object result = this.interceptor.invoke(targetInterface,\""+methodName+"\",parameterTypes,paras);");
				}
				else {
					methodBody.append("this.interceptor.invoke(targetInterface,\""+methodName+"\",parameterTypes,paras);");
				}
				
				
				methodBody.append("this.interceptor.afterInvoke();");
				
				if(returnType!=Void.TYPE) {
					methodBody.append("return ("+returnType.getName()+")result;");
				}
				
				methodBody.append("}");
				m1=CtMethod.make(methodBody.toString(), cc);
		        cc.addMethod(m1);
			}
			//cc.writeFile("/home/fenglj/devspace/myprojects/cnautosoft/H2O/myjava");
			Class<?> realClass =  cc.toClass();

	        genObject = realClass.newInstance();
	        MethodProxyNoValueReturn methodProxy = (MethodProxyNoValueReturn) MethodProxyAssist.genMethodProxy(genObject, "setInterceptor", new Class[] {Interceptor.class},false);
			methodProxy.excute(genObject, new Object[] {interceptor});
	        interfaceProxyClassMap.put(hashCode,genObject);
	        
	        cc.detach();
	        return genObject;
		}
	}
}
