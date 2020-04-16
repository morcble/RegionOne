package cn.regionsoft.one.core.aop;


public interface LifecycleInterceptor {
	void beforeInitContext();
	void afterInitContext();
}
