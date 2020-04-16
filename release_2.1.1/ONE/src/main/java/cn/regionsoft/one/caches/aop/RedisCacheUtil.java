package cn.regionsoft.one.caches.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.regionsoft.one.caches.CacheThreadData;
import cn.regionsoft.one.caches.CacheThreadHolder;
import cn.regionsoft.one.caches.LocalCacheUtil;
import cn.regionsoft.one.caches.RedisUtil;
import cn.regionsoft.one.caches.annotation.Cache;
import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.common.JsonUtil;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.data.persistence.H2OEntity;
import cn.regionsoft.one.reflect.MethodMeta;
import cn.regionsoft.one.reflect.enums.ReturnClassType;

public class RedisCacheUtil {
	protected final static String PREFIX = "${";
	protected final static String SUFFIX = "}";
	protected final static String OCCUP = "occup_";
	protected final static String ID = "id";
	
	protected final static String NIL = "nil";
	protected final static String REF = "REF_";
	protected final static String DOLLAR = "$";
	
	/**
	 * 获取占位符的信息的Map   序号-占位符名称 
	 * @param anKey
	 * @return
	 */
	public static Map<Integer,String> getOccupsInfo(String anKey) {//序号 ,属性名
		Map<Integer,String> result = new LinkedHashMap<Integer,String>();
		List<String> occupNameList = CommonUtil.getOccupationParanames(anKey, PREFIX, SUFFIX);
		String occupName = null;
		for(int i = 0 ;i <occupNameList.size();i++) {
			occupName = occupNameList.get(i);
//			if(occupName.indexOf(Constants.DOT)!=-1) {//找到嵌套属性
//				result.put(i, occupName);
//			}
			result.put(i, occupName);
		}
		return result;
	}
	
	private static Object[] resolveArgs(Cache cacheTag,Object obj,Method method,Object[] args,MethodMeta methodMeta) throws Exception {
		String methodMetaCacheKey = methodMeta.getCacheKey();
		String occupsCacheKey = OCCUP+cacheTag.key()+methodMetaCacheKey;
		Map<Integer,String> cachedOccupsInfoMap = null;
		Object embededCachedOccupsInfo = LocalCacheUtil.get(occupsCacheKey);
		if(embededCachedOccupsInfo!=null) {
			cachedOccupsInfoMap = (Map<Integer, String>) embededCachedOccupsInfo;
		}
		else {
			cachedOccupsInfoMap = getOccupsInfo(cacheTag.key());
			LocalCacheUtil.put(occupsCacheKey, cachedOccupsInfoMap);
		}
		
		//替换掉含有嵌套属性的参数
		
		Object[] filteredArgs = null;
		int occupsCount = cachedOccupsInfoMap.size();
		if(occupsCount>0) {
			filteredArgs = new Object[occupsCount];
			String embededOccupName = null;
			for(int i = 0 ; i <occupsCount;i++) {
				embededOccupName = cachedOccupsInfoMap.get(i);
				String[] fieldArray = embededOccupName.split(Constants.DOT_SPLITER);
				if(fieldArray.length>1) {
					Integer argIndex = methodMeta.getParaNames().get(fieldArray[0]);
					if(argIndex==null) {
						continue;
					}
					filteredArgs[i] = CommonUtil.getEmbededAttributeValue(args[argIndex],fieldArray);
				}
				else{
					Integer argIndex = methodMeta.getParaNames().get(fieldArray[0]);
					filteredArgs[i] = args[argIndex];
				}
			}
		}
		else {
			filteredArgs = args;
		}
		
		return new Object[] {filteredArgs,cachedOccupsInfoMap};
	}
	
	private static String calculateCacheKey(Cache cacheTag,MethodMeta methodMeta,Map<Integer,String> cachedOccupsInfoMap,Object[] filteredArgs) {
//		Map<String, Object> params = new HashMap<String,Object>();
//		Map<String,Integer> paraNames = methodMeta.getParaNames();
//		Iterator<Entry<String,Integer>> iterator = paraNames.entrySet().iterator();
//		Entry<String,Integer> tmpEntry = null;
//		int i = 0;
//		String embededOccupName = null;
//		Object tmpVal = null;
//		while(iterator.hasNext()) {
//			tmpEntry = iterator.next();
//			embededOccupName = cachedOccupsInfoMap.get(i);
//			tmpVal = filteredArgs[tmpEntry.getValue()];
//			if(tmpVal==null)tmpVal=NIL;
//			if(embededOccupName!=null) {
//				params.put(embededOccupName,tmpVal);
//			}
//			else {
//				params.put(tmpEntry.getKey(),tmpVal);
//			}
//
//			i++;
//		}
		
		Object tmpVal = null;
		Map<String, Object> params = new HashMap<String,Object>();
		for(int i = 0 ; i<filteredArgs.length;i++) {
			tmpVal = filteredArgs[i];
			if(tmpVal==null)tmpVal=NIL;
			params.put(cachedOccupsInfoMap.get(i), tmpVal);
		}
		
		String cachekey = CommonUtil.wrapText(cacheTag.key(), params);
		//System.err.println("cachekey="+cachekey);
		return cachekey;
	}
	
	//嵌套BUG
	public static Object handleCacheBeforeInvoke(Cache cacheTag,Object obj,Method method,Object[] args) throws Exception {
//		System.out.println(obj.getClass()+method.getName());
		String anKey = cacheTag.key();
		MethodMeta methodMeta =  CommonUtil.getMethodMeta(obj.getClass(), method);

		//替换掉含有嵌套属性的参数
		Object[] resolveResult = resolveArgs(cacheTag, obj, method, args,methodMeta);
		Object[] filteredArgs = (Object[]) resolveResult[0];
		Map<Integer,String> cachedOccupsInfoMap = (Map<Integer, String>) resolveResult[1];
		
		boolean hasValue = false;
		for(Object t:filteredArgs) {
			if(t!=null) {
				hasValue=true;
				break;
			}
		}
		if(!hasValue) {
			return null;//参数全为空 则忽略混存
		}
		
		
		String cacheKey = calculateCacheKey(cacheTag, methodMeta, cachedOccupsInfoMap, filteredArgs);
		Object cachedObject = null;
		if(cacheTag.action()==Cache.Action.GET) {
			String cachedJson = RedisUtil.getAndRefresh(cacheKey);
			if(!CommonUtil.isEmpty(cachedJson)) {
				if(methodMeta.getReturnClassType()==ReturnClassType.LIST) {
					Type[] typesto = methodMeta.getArgumentsOfReturnClass();
					if(typesto.length>0) {
						Class<?> genClassInList = (Class<?>) typesto[0];
						cachedObject = JsonUtil.jsonToList(cachedJson, genClassInList);
					}
				}
				else if(methodMeta.getReturnClassType()==ReturnClassType.POJO) {
					cachedObject = JsonUtil.jsonToBeanDateSerializer(cachedJson,method.getReturnType(),Constants.DATE_FORMAT1);
				}
				else {
					throw new Exception("return type is not supported for cache :"+methodMeta.getActualReturnClassType());
				}
			}
			
			if(cachedObject!=null) {
				return cachedObject;
			}
		}
		else if(cacheTag.action()==Cache.Action.REMOVE) {
			if(cacheTag.useRegex()) {
				RedisUtil.delKeysLike(cacheKey);
			}
			else {
				RedisUtil.delKey(cacheKey);
			}
		}
		else if(cacheTag.action()==Cache.Action.SELECTIVE_GET) {//特殊处理
			Map<String, Object> params = new HashMap<String,Object>();
			if(filteredArgs[0]!=null) {
				Field idField = CommonUtil.getField(filteredArgs[0].getClass(), ID);
				
				String occupName = methodMeta.getParaNames().keySet().iterator().next();
				//有ID字段
				if(idField!=null) {
					idField.setAccessible(true);
					Object idVal = idField.get(filteredArgs[0]);
					if(idVal!=null) {//ID有值的情况
						params.put(occupName,idVal);
						String idCacheKey = CommonUtil.wrapText(anKey, params);
						String cachedJson = RedisUtil.getAndRefresh(idCacheKey);
						
						if(cachedJson!=null) {
							cachedObject = JsonUtil.jsonToBeanDateSerializer(cachedJson,method.getReturnType(),Constants.DATE_FORMAT1);
							return cachedObject;  //有ID且缓存命中 则直接返回
						}
						else {//ID 有值 但是没有ID缓存 则切换为
							cacheKey = idCacheKey;
						}
					}
					else {//ID没值  查两次缓存
						//根据class 获取所有属性的key
						String classKey = CommonUtil.getAllFieldsAsString(filteredArgs[0].getClass());
						params.put(occupName,REF+classKey);
						cacheKey = CommonUtil.wrapText(anKey, params);
						
						params.clear();
						Map<String,Field> fieldsMap = CommonUtil.getAllFields(filteredArgs[0].getClass());
						Iterator<Entry<String,Field>> fieldsIterator = fieldsMap.entrySet().iterator();
						Entry<String,Field> fieldEntry = null;
						Field field = null;
						Object fieldVal = null;
						while(fieldsIterator.hasNext()) {
							fieldEntry = fieldsIterator.next();
							field = fieldEntry.getValue();
							field.setAccessible(true);
							fieldVal = field.get(filteredArgs[0]);
							if(fieldVal==null)fieldVal=NIL;
							params.put(fieldEntry.getKey(), fieldVal);
						}
						cacheKey = CommonUtil.wrapText(cacheKey, params);
						//获取IDCACHE 缓存KEY
						String cachedIdKey = RedisUtil.getAndRefresh(cacheKey);
						if(cachedIdKey!=null) {
							//获取实际缓存值
							String cachedJson = RedisUtil.getAndRefresh(cachedIdKey);
							
							if(cachedJson!=null) {
								cachedObject = JsonUtil.jsonToBeanDateSerializer(cachedJson,method.getReturnType(),Constants.DATE_FORMAT1);
								return cachedObject;  //有ID且缓存命中 则直接返回
							}
						}
					}
				}
				//被缓存对象有ID属性的时候不会到这里
				else {
					String cachedJson  = RedisUtil.getAndRefresh(cacheKey);
					if(cachedJson!=null) {
						cachedObject = JsonUtil.jsonToBeanDateSerializer(cachedJson,method.getReturnType(),Constants.DATE_FORMAT1);
						return cachedObject;  //有ID且缓存命中 则直接返回
					}
				}
			}
		}
		
		//存储到线程,以便在调用完方法的时候设置到缓存
		CacheThreadData cacheThreadData = CacheThreadHolder.getInstance().getCacheThreadData();
		cacheThreadData.put(cacheKey,cacheTag);
		
		return null;
	}
	
	public static Object handleCacheAfterInvoke(Object obj, Method method, Object[] args, Object result) throws Exception {
		CacheThreadData cacheThreadData = CacheThreadHolder.getInstance().getCacheThreadData();
		try {
			if(result==null)return null;
			Iterator<Entry<String,Cache>> iterator = cacheThreadData.iterator();
			Entry<String,Cache> tmpEntry = null;
			String cacheKey = null;
			Cache cacheTag = null;
			while(iterator.hasNext()) {
				tmpEntry = iterator.next();
				cacheKey = tmpEntry.getKey();
				cacheTag = tmpEntry.getValue();
				
				
				if(cacheTag.action()==Cache.Action.GET) {
					String objectJson = JsonUtil.objectToJsonDateSerializer(result, Constants.DATE_FORMAT1);
					setCache(cacheKey, objectJson,cacheTag.expireSeconds());
				}
				else if(cacheTag.action()==Cache.Action.PUT) {//create 的时候没有ID ,需要重新获取
					Field idField = CommonUtil.getField(method.getReturnType(), ID);
					if(idField!=null) {
						idField.setAccessible(true);
						Object idVal = idField.get(result);
						
						MethodMeta methodMeta =  CommonUtil.getMethodMeta(obj.getClass(), method);
						//替换掉含有嵌套属性的参数
						Object[] resolveResult = resolveArgs(cacheTag, obj, method, args,methodMeta);
						Object[] filteredArgs = (Object[]) resolveResult[0];
						Map<Integer,String> cachedOccupsInfoMap = (Map<Integer, String>) resolveResult[1];
						
						cacheKey = calculateCacheKey(cacheTag, methodMeta, cachedOccupsInfoMap, filteredArgs);
					}

					String objectJson = JsonUtil.objectToJsonDateSerializer(result, Constants.DATE_FORMAT1);
					setCache(cacheKey, objectJson,cacheTag.expireSeconds());
				}
				else if(cacheTag.action()==Cache.Action.SELECTIVE_GET) {//特殊处理, 缓存没有命中才会到这里
					Field idField = CommonUtil.getField(method.getReturnType(), ID);
					if(idField!=null) {
						idField.setAccessible(true);
						Object idVal = idField.get(result);
						
						MethodMeta methodMeta =  CommonUtil.getMethodMeta(obj.getClass(), method);
						Map<String,Integer> paraNames = methodMeta.getParaNames();
						Iterator<Entry<String,Integer>> paraNamesIterator = paraNames.entrySet().iterator();
						String idOccupName = paraNamesIterator.next().getKey();
						
						String objectJson = JsonUtil.objectToJsonDateSerializer(result, Constants.DATE_FORMAT1);
						if(idVal!=null) {//ID有值的情况  放入两个缓存
							Map<String, Object> params = new HashMap<String,Object>();
							params.put(idOccupName,idVal);
							String idCacheKey = CommonUtil.wrapText(cacheTag.key(), params);
							
							setCache(idCacheKey, objectJson,cacheTag.expireSeconds());//放入ID缓存
							if(!cacheKey.equals(idCacheKey))//相等的时候表示 进入selective查询id属性有值
								setCache(cacheKey, idCacheKey,cacheTag.expireSeconds());
						}
						else {//放入一个缓存
							//被缓存对象有ID属性的时候不会到这里
							setCache(cacheKey, objectJson,cacheTag.expireSeconds());
						}
					}
				}
			}
		} finally {
			
		}
		
		
		return null;
	}
	
	private static void setCache(String cacheKey, String value, int expireSeconds) {
		if(expireSeconds==0) {
			RedisUtil.setWithExpire(cacheKey,value);
		}
		else if(expireSeconds<0) {
			RedisUtil.set(cacheKey,value);
		}
		else {
			RedisUtil.set(cacheKey, value, expireSeconds);
		}
		
	}
	
	/**
	 * 放入被框架管理的缓存
	 * @param reference
	 * @param entityToCache
	 * @throws Exception
	 */
	public static void putEntityReferenceCache(H2OEntity reference,H2OEntity entityToCache) throws Exception {
		Class entityClass = reference.getClass();
		if(entityClass!=entityToCache.getClass()) {
			throw new Exception("Type is not matched");
		}
		Field idField = CommonUtil.getField(entityClass, ID);
		if(idField==null)throw new Exception("No ID field found");
		idField.setAccessible(true);
		Object idVal = idField.get(entityToCache);
		if(idVal==null) {
			throw new Exception("id of entityToCache is null");
		}
		
		String classKey = CommonUtil.getAllFieldsAsString(entityClass);
		String prefix = entityClass.getSimpleName()+Constants.COLON+REF;
		String refCacheKeyOccups = prefix+classKey;
		Map<String, Object> params = new HashMap<String,Object>();
		Map<String,Field> fieldsMap = CommonUtil.getAllFields(entityClass);
		Iterator<Entry<String,Field>> fieldsIterator = fieldsMap.entrySet().iterator();
		Entry<String,Field> fieldEntry = null;
		Field field = null;
		Object fieldVal = null;
		while(fieldsIterator.hasNext()) {
			fieldEntry = fieldsIterator.next();
			field = fieldEntry.getValue();
			field.setAccessible(true);
			fieldVal = field.get(reference);
			if(fieldVal==null)fieldVal=NIL;
			params.put(fieldEntry.getKey(), fieldVal);
		}
		String refCacheKey = CommonUtil.wrapText(refCacheKeyOccups, params);
		String idCacheKey = entityClass.getSimpleName()+Constants.COLON+idVal ;
		String objectJson = JsonUtil.objectToJson(entityToCache);
		
		Map<String,String> valueToCache = new HashMap<String,String>();
		valueToCache.put(idCacheKey,objectJson);
		valueToCache.put(refCacheKey,idCacheKey);
		RedisUtil.setTranMapWithExpire(valueToCache);
//		
//		RedisUtil.setWithExpire(idCacheKey,objectJson);//放入ID缓存
//		RedisUtil.setWithExpire(refCacheKey,idCacheKey);
		
	}
}
