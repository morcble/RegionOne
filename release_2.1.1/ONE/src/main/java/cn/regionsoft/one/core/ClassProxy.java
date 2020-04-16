package cn.regionsoft.one.core;

public interface ClassProxy {
	 public Object proxyCglibClass(Class<?> targetClass);
	 public Object proxyObject(Object targetObject);
}
