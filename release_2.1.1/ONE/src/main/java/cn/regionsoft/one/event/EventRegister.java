package cn.regionsoft.one.event;

import java.util.HashSet;
import java.util.Map;
import cn.regionsoft.one.annotation.Component;
import cn.regionsoft.one.annotation.InstanceAnoType;
import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.core.SystemContext;
import cn.regionsoft.one.core.aop.ApplicationListener;

@Component
public class EventRegister implements ApplicationListener{
	@Override
	public void onApplicationInited(SystemContext systemContext) {
		EventFactory eventFactory = EventFactory.getInstance();

		for(H2OContext context : systemContext.getContextsMap().values()){
			Map<Class<?>,Object> classInstanceMap = context.getClassInstanceMap();
			
			HashSet<Class<?>> quickEventListeners = context.getAnnotatedClassHub().getClassSetByAnnotation(InstanceAnoType.QuickEventListener);
			if(quickEventListeners!=null) {
				for(Class<?> listenerClass:quickEventListeners) {
					eventFactory.register(listenerClass.getName(), (EventListener)(classInstanceMap.get(listenerClass)));
				}
			}
			
			HashSet<Class<?>> slowEventListeners = context.getAnnotatedClassHub().getClassSetByAnnotation(InstanceAnoType.SlowEventListener);
			if(slowEventListeners!=null) {
				for(Class<?> listenerClass:slowEventListeners) {
					eventFactory.register(listenerClass.getName(), (EventListener)(classInstanceMap.get(listenerClass)));
				}
			}

		}
	}

	
}