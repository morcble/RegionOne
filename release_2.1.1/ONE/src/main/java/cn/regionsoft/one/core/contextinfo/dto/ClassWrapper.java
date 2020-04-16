package cn.regionsoft.one.core.contextinfo.dto;


public class ClassWrapper implements Comparable<ClassWrapper>{
	public ClassWrapper(Class beanClass) {
		super();
		this.beanClass = beanClass;
		this.className = beanClass.getSimpleName();
	}
	private Class beanClass = null;
	private String className = null;
	public Class getBeanClass() {
		return beanClass;
	}
	public void setBeanClass(Class beanClass) {
		this.beanClass = beanClass;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	
	@Override
	public int compareTo(ClassWrapper arg0) {
		return this.getClassName().compareTo(arg0.getClassName());
	}
	
}
