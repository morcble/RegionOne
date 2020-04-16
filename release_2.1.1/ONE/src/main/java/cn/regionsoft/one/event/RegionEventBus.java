package cn.regionsoft.one.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import cn.regionsoft.one.annotation.QuickEventListener;
import cn.regionsoft.one.annotation.SlowEventListener;
import cn.regionsoft.one.tool.ThreadPool;

public class RegionEventBus{

	private Map<String,EventListener> listenerMap = new ConcurrentHashMap<String,EventListener>();

	public RegionEventBus() {
		super();
	}
	
	public void register(String listenerClassName,EventListener baseEventListener) {
		listenerMap.put(listenerClassName, baseEventListener);
	}
	
	public void unregister(String listenerClassName) {
		listenerMap.remove(listenerClassName);
	}
	
	/*
	 * 异步事件
	 */
	public Future post(BaseEvent event) {
		Class<?> enclosingClass = event.getClass().getEnclosingClass();
		
		EventListener eventListener = listenerMap.get(enclosingClass.getName());
		if(eventListener==null)return null;
		
		EventTask task = new EventTask(event,eventListener);
		if(enclosingClass.isAnnotationPresent(QuickEventListener.class)) {
			return ThreadPool.getInstance().submitQuickTask(task);
		}
		else if(enclosingClass.isAnnotationPresent(SlowEventListener.class)) {
			return ThreadPool.getInstance().submitSlowTask(task);
		}
		
		return null;
	}
	
	/*
	 * 同步事件
	 */
	public Object postSync(BaseEvent event) {
		Class<?> enclosingClass = event.getClass().getEnclosingClass();
		
		EventListener eventListener = listenerMap.get(enclosingClass.getName());
		if(eventListener==null)return null;
		
		return eventListener.handleEvent(event);
	}
}
