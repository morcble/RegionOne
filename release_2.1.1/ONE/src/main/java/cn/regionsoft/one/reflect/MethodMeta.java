package cn.regionsoft.one.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import cn.regionsoft.one.reflect.enums.ReturnClassType;

public class MethodMeta {
	/**
	 * 方法参数map
	 */
	private Map<String,Integer> paraNames;
	
	private Map<Integer,String> paraNamesMirror;
	
	/**
	 * 返回类型
	 */
	private ReturnClassType returnClassType;
	
	/**
	 * 返回类型
	 */
	private Type actualReturnClassType;
	
	/**
	 * 泛型
	 */
	private Type[] argumentsOfReturnClass;
	
	private Class<?>[] parameterTypes;
	private Annotation[][] parameterAnnotations;
	
	//计算出来的缓存key
	private String cacheKey;

	public MethodMeta(Map<String, Integer> paraNames, ReturnClassType returnClassType, 
			Type[] argumentsOfReturnClass,Type actualReturnClassType,String cacheKey,
			Class<?>[] parameterTypes,Annotation[][] parameterAnnotations) {
		super();
		this.paraNames = paraNames;
		
		if(paraNames!=null) {
			paraNamesMirror = new HashMap<Integer,String>();
			Iterator<Entry<String, Integer>> iterator = paraNames.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<String, Integer> tmp = iterator.next();
				paraNamesMirror.put(tmp.getValue(), tmp.getKey());
			}
		}
		this.returnClassType = returnClassType;
		this.argumentsOfReturnClass = argumentsOfReturnClass;
		this.actualReturnClassType = actualReturnClassType;
		this.cacheKey = cacheKey;
		this.parameterTypes = parameterTypes;
		this.parameterAnnotations = parameterAnnotations;
	}

	public Map<Integer, String> getParaNamesMirror() {
		return paraNamesMirror;
	}

	public Map<String, Integer> getParaNames() {
		return paraNames;
	}

	public ReturnClassType getReturnClassType() {
		return returnClassType;
	}

	public Type[] getArgumentsOfReturnClass() {
		return argumentsOfReturnClass;
	}

	public Type getActualReturnClassType() {
		return actualReturnClassType;
	}

	public String getCacheKey() {
		return cacheKey;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	public Annotation[][] getParameterAnnotations() {
		return parameterAnnotations;
	}
}
