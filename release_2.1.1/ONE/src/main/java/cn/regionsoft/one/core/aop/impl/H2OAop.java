package cn.regionsoft.one.core.aop.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.regionsoft.one.caches.LocalCacheUtil;
import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.ClassProxy;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.DBType;
import cn.regionsoft.one.core.H2OCloudDaoI;
import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.core.H2ODaoI;
import cn.regionsoft.one.core.SystemContext;
import cn.regionsoft.one.core.aop.AOPListener;
import cn.regionsoft.one.core.aop.AnnotationListener;
import cn.regionsoft.one.core.aop.MethodListener;
import cn.regionsoft.one.core.dbconnection.CloudConnectionManager;
import cn.regionsoft.one.core.dbconnection.SQLConnectionManager;
import cn.regionsoft.one.core.entity.CloudDBEntityManager;
import cn.regionsoft.one.core.entity.SQLEntityManager;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;


/**
 * @author fenglj
 *
 */
public class H2OAop implements MethodInterceptor,ClassProxy { 
	public static Logger logger = Logger.getLogger(H2OAop.class);
	
    @Override  
    public Object proxyCglibClass(Class<?> targetClass) {  
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(targetClass);
		enhancer.setCallback(this);
		return enhancer.create();
    }  
    
    private final String DAO_CLASS = "cn.regionsoft.one.core.H2ODao";
    private final String ENHANCED_TAG = "$$";
    @Override  
    public Object intercept(Object obj, java.lang.reflect.Method method, Object[] args, MethodProxy proxy) throws Throwable {  
    	List<AOPListener> matchedListeners = null;
    	Object result = null;
    	H2OContext h2oContext = null;
    	
    	boolean operateConnection = false;
    	try{
    		String proxyClassName = obj.getClass().getName();
    		String origClassName = proxyClassName.substring(0,proxyClassName.indexOf(ENHANCED_TAG));
    		
    		//Class<?> orignalClass = Class.forName(origClassName);
    		h2oContext = CommonUtil.getTargetContextByClassName(origClassName);
    		
    		matchedListeners = getMatchedListeners(h2oContext,origClassName,method);
    		
    		if(obj instanceof H2ODaoI ||obj instanceof H2OCloudDaoI) {
    			operateConnection = true;
//    			StackTraceElement[] stacks =  Thread.currentThread().getStackTrace();
//    			if(stacks.length>3 && !DAO_CLASS.equals(stacks[3].getClassName())) {
//    				logger.debug(stacks[3].getClassName()+Constants.DOT+stacks[3].getMethodName()+Constants.MINUS+stacks[3].getLineNumber());
//    			}
    		}
    		
    		
    		if(operateConnection) {
    			if(h2oContext.getConfig().getDbType() == DBType.MONGODB) {
        			
        		}
    			else if(h2oContext.getConfig().getDbType() == DBType.CLOUDDB) {
    				CloudDBEntityManager.getConnection(h2oContext);
        		}
        		else {
        			SQLEntityManager.getConnection(h2oContext);
        		}
    		}
    		
   
    		for(AOPListener aopListener:matchedListeners){
    			Object repalceObject = aopListener.beforeInvoke(obj, method, args,h2oContext);	
    			//任何一个listener返回替换的结果 就直接返回
    			if(repalceObject!=null)
    				return repalceObject;
    		}
    		
    		result = proxy.invokeSuper(obj, args); 
    		
    		for(AOPListener aopListener:matchedListeners){
    			aopListener.afterInvoke(obj, method, args, result,h2oContext);
    		}
    		return result;  
    	}
    	catch(Throwable e){
    		for(AOPListener aopListener:matchedListeners){
    			aopListener.exceptionInvoke(e,h2oContext);
    		}
    		throw e;
    	}
    	finally{
    		if(operateConnection) {
    			if(h2oContext.getConfig().getDbType() == DBType.MONGODB) {
        			
        		}
    			else if(h2oContext.getConfig().getDbType() == DBType.CLOUDDB) {
    				CloudConnectionManager.releaseConnection(h2oContext);
        		}
        		else {
        			SQLConnectionManager.releaseConnection(h2oContext);
        		}
    		}
    		
    		for(AOPListener aopListener:matchedListeners){
    			aopListener.finalInvoke(obj, method, args, result,h2oContext);//不定积分常用基本技巧
    		}
    	}
    }  
    
    private static List<AOPListener> getMatchedListeners(H2OContext h2oContext, String classFullName, Method method){
    	String cacheKey = LocalCacheUtil.GET_MATCHED_LISTENERS+classFullName+Constants.MINUS +method.getName();
    	Object cached = LocalCacheUtil.get(cacheKey);
    	if(cached!=null) {
    		return (List<AOPListener>) cached;
    	}
    	
    	List<AOPListener> ls = new ArrayList<AOPListener>();
    	
    	List<AOPListener> aopListeners = h2oContext.getAopListeners();
    	for(AOPListener aopListener:aopListeners){
    		if(aopListener instanceof MethodListener){//方法监听器
    			MethodListener tmp = (MethodListener) aopListener;
    			Pattern pattern = Pattern.compile(tmp.getMethodRegEx());
    			
    			String matchKey = classFullName + Constants.DOT+ method.getName();
    			Matcher matcher = pattern.matcher(matchKey);
    			boolean rs = matcher.matches();
    			if(rs){
    				ls.add(aopListener);// check whether method matched regEx
    			}
    		}
    		else if(aopListener instanceof AnnotationListener){//注解监听器
    			AnnotationListener tmp = (AnnotationListener) aopListener;
    			String a = method.getName();
    			String b = classFullName;
    			Annotation annotation = method.getAnnotation(tmp.targetAnnotation());
    			
//    			if(annotation==null){
//    				Method impMethod = getMatchedMethod(orignalClass,method);
//    				annotation = impMethod.getAnnotation(tmp.targetAnnotation());
//    			}
    			
    			if(annotation!=null){
    				ls.add(aopListener);
    			}
    		}
    	}
    	
    	LocalCacheUtil.put(cacheKey, ls);
    	return ls;
    }
    
    
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
    
    private static boolean isArrayMatched(Class<?>[] classArray1,Class<?>[] classArray2){
    	if(classArray1==null)classArray1 = new Class[0];
    	if(classArray2==null)classArray2 = new Class[0];
    	for(int i = 0; i <classArray1.length ; i ++){
    		if(classArray1[i]!=classArray2[i]){
    			return false;
    		}
    	}
    	return true;
    }
    
	@Override
	public Object proxyObject(Object targetObject) {
		
		return null;
	}
  
}