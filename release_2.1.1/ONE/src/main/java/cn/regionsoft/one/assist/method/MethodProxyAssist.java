package cn.regionsoft.one.assist.method;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class MethodProxyAssist {
	private static Map<Integer,MethodProxy> methodProxyClassMap = new ConcurrentHashMap<Integer,MethodProxy>();
	/*
	 * hasReturnVal = true   ,return MethodProxyWithValueReturn
	 * hasReturnVal = false  ,return MethodProxyNoValueReturn
	 * 
	 * @param srcObj
	 * @param methodName
	 * @param parameterTypes
	 * @param hasReturnVal
	 * @return
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public static MethodProxy genMethodProxy(Object srcObj,String methodName,Class<?>[] parameterTypes,boolean hasReturnVal) throws CannotCompileException, NotFoundException, InstantiationException, IllegalAccessException {
		int hashCode = srcObj.getClass().getName().hashCode()*31 +methodName.hashCode();
		
		if(parameterTypes!=null) {
			for(Class<?> tmp:parameterTypes) {
				hashCode = hashCode*31+tmp.getName().hashCode();
			}
		}
		
		boolean flag = false;
		if(hashCode<0) {
			hashCode=-hashCode;
			flag = true;
		}
		
		MethodProxy cachedProxy = (MethodProxy) methodProxyClassMap.get(hashCode);
		if(cachedProxy!=null) {
			return cachedProxy;
		}
		
		synchronized(MethodProxyAssist.class) {
			cachedProxy =  methodProxyClassMap.get(hashCode);
			if(cachedProxy!=null) return cachedProxy;
			
			String className = srcObj.getClass().getName();	
			ClassPool pool = ClassPool.getDefault();
	        CtClass cc = null;
	        if(flag) {
	        	cc = pool.makeClass(className+"_"+methodName+"Proxy__"+hashCode);
	        }
	        else {
	        	cc = pool.makeClass(className+"_"+methodName+"Proxy_"+hashCode);
	        }
	        if(hasReturnVal)
	        	cc.addInterface(pool.get("cn.regionsoft.one.assist.method.MethodProxyWithValueReturn"));
	        else
	        	cc.addInterface(pool.get("cn.regionsoft.one.assist.method.MethodProxyNoValueReturn"));
	        
	        StringBuilder methodBody = null;
	        if(hasReturnVal) 
	        	methodBody = new StringBuilder("public Object excute(Object obj,Object[] p) {");
	        else
	        	methodBody = new StringBuilder("public void excute(Object obj,Object[] p) {");
	        
	        methodBody.append(className);
	        methodBody.append(" ");
	        methodBody.append(" t = (");
	        methodBody.append(className);
	        methodBody.append(")obj;");
	        if(hasReturnVal) 
	        	methodBody.append("return t.");
	        else 
	        	methodBody.append("t.");
	        methodBody.append(methodName+"(");
	        if(parameterTypes!=null) {
	        	int length = parameterTypes.length;
	        	for(int i = 0 ; i <length ; i++) {
	        		methodBody.append("(");
	        		methodBody.append(parameterTypes[i].getName());
	        		methodBody.append(")p["+i+"]");
	        		if(i!=(length-1)) {
	        			methodBody.append(",");
	        		}
	        	}
	        }
	        methodBody.append(");}");
	        
	        CtMethod m1=CtMethod.make(methodBody.toString(), cc);
	        cc.addMethod(m1);
	        
			Class<?> realClass = (Class<?>) cc.toClass();
	        cc.detach();
	        
	        MethodProxy newInstance = (MethodProxy) realClass.newInstance();
	        methodProxyClassMap.put(hashCode,newInstance);
	        return newInstance;
		}
		
	}	
	
	/*
	public static MethodProxyNoValueReturn genMethodProxyNoValueReturn(Object srcObj,String methodName,Class<?>[] parameterTypes) throws CannotCompileException, NotFoundException, InstantiationException, IllegalAccessException {
		int hashCode = srcObj.getClass().getName().hashCode()*31 +methodName.hashCode();
		
		if(parameterTypes!=null) {
			for(Class<?> tmp:parameterTypes) {
				hashCode = hashCode*31+tmp.getName().hashCode();
			}
		}
		
		boolean flag = false;
		if(hashCode<0) {
			hashCode=-hashCode;
			flag = true;
		}
		
		MethodProxyNoValueReturn cachedProxy = (MethodProxyNoValueReturn) methodProxyClassMap.get(hashCode);
		if(cachedProxy!=null) {
			return cachedProxy;
		}
		synchronized(MethodProxyAssist.class) {
			cachedProxy = (MethodProxyNoValueReturn) methodProxyClassMap.get(hashCode);
			if(cachedProxy!=null) return cachedProxy;
			
			String className = srcObj.getClass().getName();	
			ClassPool pool = ClassPool.getDefault();
	        CtClass cc = null;
	        if(flag) {
	        	cc = pool.makeClass(className+"_"+methodName+"Proxy__"+hashCode);
	        }
	        else {
	        	cc = pool.makeClass(className+"_"+methodName+"Proxy_"+hashCode);
	        }
	        cc.addInterface(pool.get("cn.regionsoft.one.assist.method.MethodProxyNoValueReturn"));
	        StringBuilder methodBody = null;
	        
	        methodBody = new StringBuilder("public void excute(Object obj,Object[] p) {");
	        
	        methodBody.append(className);
	        methodBody.append(" ");
	        methodBody.append(" t = (");
	        methodBody.append(className);
	        methodBody.append(")obj;");
	        methodBody.append("t.");
	        methodBody.append(methodName+"(");
	        if(parameterTypes!=null) {
	        	int length = parameterTypes.length;
	        	for(int i = 0 ; i <length ; i++) {
	        		methodBody.append("(");
	        		methodBody.append(parameterTypes[i].getName());
	        		methodBody.append(")p["+i+"]");
	        		if(i!=(length-1)) {
	        			methodBody.append(",");
	        		}
	        	}
	        }
	        methodBody.append(");}");
	        
	        CtMethod m1=CtMethod.make(methodBody.toString(), cc);
	        cc.addMethod(m1);
	        
			Class<?> realClass = (Class<?>) cc.toClass();
	        cc.detach();
	        
	        MethodProxyNoValueReturn newInstance = (MethodProxyNoValueReturn) realClass.newInstance();
	        methodProxyClassMap.put(hashCode,newInstance);
	        return newInstance;
		}
		
	}	*/
}


