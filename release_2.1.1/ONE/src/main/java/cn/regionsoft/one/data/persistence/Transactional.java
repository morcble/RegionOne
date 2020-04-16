package cn.regionsoft.one.data.persistence;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;

@Target({java.lang.annotation.ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Transactional {
	Class<? extends Throwable> rollBackFor() default Throwable.class;
	//isolation
}
