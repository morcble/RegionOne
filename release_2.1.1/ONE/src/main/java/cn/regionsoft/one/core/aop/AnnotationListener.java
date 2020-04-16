package cn.regionsoft.one.core.aop;

import java.lang.annotation.Annotation;

public interface AnnotationListener extends AOPListener{
	public Class<? extends Annotation> targetAnnotation();
}
