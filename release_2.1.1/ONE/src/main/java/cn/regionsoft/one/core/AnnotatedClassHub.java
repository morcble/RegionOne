package cn.regionsoft.one.core;


import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cn.regionsoft.one.annotation.InstanceAnoType;
import cn.regionsoft.one.annotation.NoInstanceAnoType;

public class AnnotatedClassHub {
	/**
	 * Annotation class name - metadata
	 * cn.regionsoft.xframe.annotation.Autowired - framework class
	 */
	private Map<Class<? extends Annotation>,HashSet<Class<?>>> noInstanceMap = new HashMap<Class<? extends Annotation>,HashSet<Class<?>>>();
	private Map<Class<? extends Annotation>,HashSet<Class<?>>> instanceMap = new HashMap<Class<? extends Annotation>,HashSet<Class<?>>>();
	
	private boolean isInited = false;

	/**
	 * get framework managed class by filtering all the context classes
	 * only can be invoked one time
	 * @param contextClasses
	 * @return
	 */
	public Set<Class<?>> init(HashSet<Class<?>> contextClasses,String contextName) {
		Set<Class<?>> needInstanceClass = new HashSet<Class<?>>();
		if(!isInited){
			isInited = true;
			for(Class<?> tmp:contextClasses){
				if(contextName.equals(CommonUtil.getTargetContextName(tmp)))
					resolveXFrameAno(tmp,needInstanceClass);
			}
		}
		return needInstanceClass;
	}

	private void resolveXFrameAno(Class<?> classType,Set<Class<?>> needInstanceClassPara){
		Class<? extends Annotation> anoClass = null;
		Annotation annotation = null;
		for (InstanceAnoType s : InstanceAnoType.values())  {
			annotation = classType.getDeclaredAnnotation(s.getClassType());
			if(annotation!=null){
				anoClass= annotation.annotationType();
				needInstanceClassPara.add(classType);
				
				HashSet<Class<?>> classSet = instanceMap.get(anoClass);
				if(classSet==null){
					classSet = new HashSet<Class<?>>();
					instanceMap.put(anoClass, classSet);
				}
				classSet.add(classType);
				
				resolveXFrameAno(anoClass,needInstanceClassPara);
			}
		}  
		
		for (NoInstanceAnoType s : NoInstanceAnoType.values())  {
			annotation = classType.getDeclaredAnnotation(s.getClassType());
			if(annotation!=null){
				anoClass= annotation.annotationType();
				HashSet<Class<?>> classSet = noInstanceMap.get(anoClass);
				if(classSet==null){
					classSet = new HashSet<Class<?>>();
					noInstanceMap.put(anoClass, classSet);
				}
				classSet.add(classType);
				resolveXFrameAno(anoClass,needInstanceClassPara);
			}
		}
	}
	
	public HashSet<Class<?>> getClassSetByAnnotation(InstanceAnoType instanceAnoType){
		return instanceMap.get(instanceAnoType.getClassType());
	}
	
	public HashSet<Class<?>> getClassSetByAnnotation(NoInstanceAnoType noInstanceAnoType){
		return noInstanceMap.get(noInstanceAnoType.getClassType());
	}
	
	/*TODO REMOVE
	 * public boolean isClassHasAnnotation(Class<?> classType, InstanceAnoType instanceAnoType){
		HashSet<Class<?>> tmpSet = instanceMap.get(instanceAnoType.getClassType());
		if(tmpSet!=null){
			return tmpSet.contains(classType);
		}
		return false;
	}
	
	public boolean isClassHasAnnotation(Class<?> classType, NoInstanceAnoType noInstanceAnoType){
		HashSet<Class<?>> tmpSet = noInstanceMap.get(noInstanceAnoType.getClassType());
		if(tmpSet!=null){
			return tmpSet.contains(classType);
		}
		return false;
	}
	*/

}
