package cn.regionsoft.one.annotation;
import java.lang.annotation.Annotation;

public enum NoInstanceAnoType {
	/*
	 * class level------------------
	 * no need instance
	 */
	Entity(cn.regionsoft.one.data.persistence.Entity.class);
	
	
	private Class<? extends Annotation> classType;
	private NoInstanceAnoType(Class<? extends Annotation> classType) {
		this.classType = classType;
	}
	public Class<? extends Annotation> getClassType() {
		return classType;
	}
	

}
