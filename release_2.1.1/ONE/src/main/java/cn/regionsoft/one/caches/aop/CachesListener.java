package cn.regionsoft.one.caches.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import cn.regionsoft.one.caches.CacheThreadHolder;
import cn.regionsoft.one.caches.RedisUtil;
import cn.regionsoft.one.caches.annotation.Cache;
import cn.regionsoft.one.caches.annotation.Caches;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.core.aop.AnnotationListener;

public class CachesListener implements AnnotationListener{
	private static final Logger logger = Logger.getLogger(CachesListener.class);
	
	public Class<? extends Annotation> targetAnnotation(){
		return Caches.class;
	}
	
	public Object beforeInvoke(Object obj, java.lang.reflect.Method method, Object[] args,H2OContext h2oContext){
		CacheThreadHolder.getInstance().getCacheThreadData().pushStack();
		
		if(!RedisUtil.isInited()) {//redis如果没有设置成功,则忽略缓存注解
			return null;
		}
		
		try{
			Caches caches = method.getAnnotation(Caches.class);
			Cache[] cacheArray = caches.value();
			Object resultObj = null;
			for(Cache cacheTag:cacheArray) {
				Object tmp = RedisCacheUtil.handleCacheBeforeInvoke(cacheTag, obj, method, args);
				if(resultObj!=null&&tmp!=null) {
					resultObj = tmp;
				}
			}
			return resultObj;
    	}
    	catch(Throwable ex){
    		logger.error(ex);
    		return null;
    	}
    	finally{
    		
    	}
	}
	
	public Object afterInvoke(Object obj, Method method, Object[] args, Object result,H2OContext h2oContext){
		try{
			RedisCacheUtil.handleCacheAfterInvoke(obj, method, args, result);
    		return null;
    	}
    	catch(Throwable ex){
    		logger.error(ex);
    		return null;
    	}

	}

	@Override
	public void exceptionInvoke(Throwable e, H2OContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finalInvoke(Object obj, Method method, Object[] args, Object result, H2OContext context) {
		// TODO Auto-generated method stub
		CacheThreadHolder.getInstance().getCacheThreadData().popStack();
	}
}
