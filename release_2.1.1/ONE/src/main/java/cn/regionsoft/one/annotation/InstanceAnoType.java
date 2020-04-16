package cn.regionsoft.one.annotation;
import java.lang.annotation.Annotation;

public enum InstanceAnoType {
	/*
	 * class level------------------
	 * need instance
	 */
	Controller(Controller.class),
	Component(Component.class),
	Batch(Batch.class),
	Resource(Resource.class),
	Service(Service.class),
	Dao(Dao.class),
	QuickEventListener(QuickEventListener.class),
	SlowEventListener(SlowEventListener.class);

	
	private Class<? extends Annotation> classType;
	private InstanceAnoType(Class<? extends Annotation> classType) {
		this.classType = classType;
	}
	public Class<? extends Annotation> getClassType() {
		return classType;
	}
	

}
