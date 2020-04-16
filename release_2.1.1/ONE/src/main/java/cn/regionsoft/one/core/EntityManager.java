package cn.regionsoft.one.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.regionsoft.one.core.entity.BindObject;
import cn.regionsoft.one.data.dialet.core.Dialet;

public abstract class EntityManager {

	public abstract Dialet getDialet();

	public abstract void init();
	
	/**
	 * Static ,entity class to table map cache
	 */
	private Map<Class<?>,BindObject> entityToTableCache =  new ConcurrentHashMap<Class<?>,BindObject>();
	
	/**
	 * get bindobject by entity class
	 * @param classType
	 * @return
	 */
	public BindObject getBindObject(Class<?> classType){
		BindObject result = entityToTableCache.get(classType);
		if(result==null){
			result = new BindObject(classType);
			entityToTableCache.put(classType, result);
		}
		return result;
	}

	public Map<Class<?>, BindObject> getEntityToTableCache() {
		return entityToTableCache;
	}

}
