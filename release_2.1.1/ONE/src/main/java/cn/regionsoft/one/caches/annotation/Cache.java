package cn.regionsoft.one.caches.annotation;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({java.lang.annotation.ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Caches.class)
public @interface Cache {
	String key();
	Action action();
	boolean useRegex() default false; 
	int expireSeconds() default 0;//0:读配置redis.expire.default; -1:永久; 1000:1000秒

	public static enum Action {
		/**
		 * 根据ID更新
		 */
		PUT,
		/**
		 * 根据ID删除
		 */
		REMOVE,
		/**
		 * 根据ID查询
		 */
		GET,
		/**
		 * 参数为ENTITY
		 */
		SELECTIVE_GET
	}
}


