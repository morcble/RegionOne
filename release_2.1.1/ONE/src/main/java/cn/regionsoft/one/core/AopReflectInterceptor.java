//package cn.regionsoft.one.core;
//
//import java.lang.annotation.Annotation;
//import java.lang.reflect.InvocationHandler;
//import java.lang.reflect.Method;
//import java.lang.reflect.Proxy;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import cn.regionsoft.one.core.aop.AOPListener;
//import cn.regionsoft.one.core.aop.AnnotationListener;
//import cn.regionsoft.one.core.aop.MethodListener;
//
//public class AopReflectInterceptor implements ClassProxy {   
//	@Override
//	public Object proxyObject(Object targetObj) {
//		Class<?> targetClass = targetObj.getClass();
//		Class<?>[] interfaces = targetClass.getInterfaces();
//		if(interfaces.length==0) return targetObj;
//		return Proxy.newProxyInstance(targetClass.getClassLoader(), interfaces, new AopReflectInterceptor.Handler(targetObj));   
//	}   
//
//    public static class Handler implements InvocationHandler {
//    	private Object targetObj;
//    	
//		public Handler(Object targetObj) {
//			this.targetObj = targetObj;
//		}
//
//		@Override
//		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//			List<AOPListener> matchedListeners = null;
//	    	Object result = null;
//	    	H2OContext h2oContext = null;
//	    	
//	    	try{
//	    		String proxyClassName = targetObj.getClass().getName();
//	    		Class<?> orignalClass = Class.forName(proxyClassName);
//	    		h2oContext = CommonUtil.getTargetContext(orignalClass);
//	    		
//	    		matchedListeners = getMatchedListeners(h2oContext,orignalClass,method);
//	    		
//	    		for(AOPListener aopListener:matchedListeners){
//	    			aopListener.beforeInvoke(targetObj, method, args,h2oContext);
//	    		}
//	    		/**
//	    		 * method invoked here
//	    		 */
//	    		result = method.invoke(targetObj, args);
//	    		
//	    		for(AOPListener aopListener:matchedListeners){
//	    			aopListener.afterInvoke(targetObj, method, args, result,h2oContext);
//	    		}
//	    		
//	    		return result;  
//	    	}
//	    	catch(Throwable e){
//	    		for(AOPListener aopListener:matchedListeners){
//	    			aopListener.exceptionInvoke(e,h2oContext);
//	    		}
//	    		throw e.getCause();
//	    	}
//	    	finally{
//	    		for(AOPListener aopListener:matchedListeners){
//	    			try{
//	    				aopListener.finalInvoke(targetObj, method, args, result,h2oContext);
//	    			}
//	    			catch(Exception e){
//	    				e.printStackTrace();
//	    			}
//	    			
//	    		}
//	    	}
//
//		}
//
//    }
//    
//    private static List<AOPListener> getMatchedListeners(H2OContext h2oContext, Class<?> orignalClass, Method method){
//    	List<AOPListener> ls = new ArrayList<AOPListener>();
//    	
//    	List<AOPListener> aopListeners = h2oContext.getAopListeners();
//    	for(AOPListener aopListener:aopListeners){
//    		if(aopListener instanceof MethodListener){
//    			MethodListener tmp = (MethodListener) aopListener;
//    			Pattern pattern = Pattern.compile(tmp.getMethodRegEx());
//    			
//    			String matchKey = orignalClass.getName() + "."+ method.getName();
//    			Matcher matcher = pattern.matcher(matchKey);
//    			boolean rs = matcher.matches();
//    			if(rs){
//    				ls.add(aopListener);// check whether method matched regEx
//    			}
//    		}
//    		else if(aopListener instanceof AnnotationListener){
//    			AnnotationListener tmp = (AnnotationListener) aopListener;
//    			Annotation annotation = method.getAnnotation(tmp.targetAnnotation());
//    			
//    			if(annotation==null){
//    				Method impMethod = getMatchedMethod(orignalClass,method);
//    				annotation = impMethod.getAnnotation(tmp.targetAnnotation());
//    			}
//    			
//    			if(annotation!=null){
//    				ls.add(aopListener);
//    			}
//    		}
//    	}
//    	return ls;
//    }
//    
//    private static Method getMatchedMethod(Class<?> implClass,Method interfaceMethod){
//    	Method[] methodArray = implClass.getMethods();
//    	for(Method tmpMethod:methodArray){
//    		if(tmpMethod.getName().equals(interfaceMethod.getName())){
//    			if(isArrayMatched(tmpMethod.getParameterTypes(),interfaceMethod.getParameterTypes())){
//    				if(tmpMethod.getReturnType()==interfaceMethod.getReturnType()){
//    					return tmpMethod;
//    				}
//    			}
//    		}
//    	}
//    	return null;
//    }
//    
//    private static boolean isArrayMatched(Class<?>[] classArray1,Class<?>[] classArray2){
//    	if(classArray1==null)classArray1 = new Class[0];
//    	if(classArray2==null)classArray2 = new Class[0];
//    	for(int i = 0; i <classArray1.length ; i ++){
//    		if(classArray1[i]!=classArray2[i]){
//    			return false;
//    		}
//    	}
//    	return true;
//    }
//
//	@Override
//	public Object proxyCglibClass(Class<?> targetClass) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}