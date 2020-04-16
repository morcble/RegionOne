package cn.regionsoft.one.caches.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import cn.regionsoft.one.caches.CacheThreadHolder;
import cn.regionsoft.one.caches.RedisUtil;
import cn.regionsoft.one.caches.annotation.Cache;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.core.aop.AnnotationListener;

public class CacheListener implements AnnotationListener{
	private static final Logger logger = Logger.getLogger(CacheListener.class);
	
	public Class<? extends Annotation> targetAnnotation(){
		return Cache.class;
	}
	
	@SuppressWarnings("unchecked")
	public Object beforeInvoke(Object obj, Method method, Object[] args,H2OContext h2oContext){
		if(!RedisUtil.isInited()) {//redis如果没有设置成功,则忽略缓存注解
			return null;
		}
		
		CacheThreadHolder.getInstance().getCacheThreadData().pushStack();
		try{
			Cache cacheTag = method.getAnnotation(Cache.class);
			return RedisCacheUtil.handleCacheBeforeInvoke(cacheTag, obj, method, args);
    	}
    	catch(Throwable ex){
    		logger.error(ex);
    		return null;
    	}
	}
	

	public Object afterInvoke(Object obj, Method method, Object[] args, Object result,H2OContext h2oContext){
		try{
			if(!RedisUtil.isInited()) {//redis如果没有设置成功,则忽略缓存注解
				return null;
			}
			
			RedisCacheUtil.handleCacheAfterInvoke(obj, method, args, result);
    		return null;
    	}
    	catch(Throwable ex){
    		logger.error(ex);
    		return null;
    	}

	}

	@Override
	public void exceptionInvoke(Throwable e,H2OContext h2oContext) {
		try{
			
    	}
    	catch(Throwable ex){
    		logger.error(ex);
    	}
	}

	@Override
	public void finalInvoke(Object obj, Method method, Object[] args, Object result,
			H2OContext h2oContext) {
		if(!RedisUtil.isInited()) {//redis如果没有设置成功,则忽略缓存注解
			return;
		}
		try{
			CacheThreadHolder.getInstance().getCacheThreadData().popStack();
    	}
    	catch(Throwable ex){
    		logger.error(ex);
    	}
	}
	

}
