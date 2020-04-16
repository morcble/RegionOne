package cn.regionsoft.one.annotation.tag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.regionsoft.one.enums.RequestMethod;

@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RequestMapping {

	String value() default "";

	String[] responseHeader() default "Content-Type==application/json;charset=UTF-8";

	RequestMethod[] method() default {RequestMethod.POST,RequestMethod.GET};
	
	//是否忽略框架加密
	boolean ignoreEncryption() default false;

}
