package cn.regionsoft.one.event;

import java.lang.reflect.Method;

import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.reflect.MethodMeta;
import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.Subscribe;

public abstract class BaseEventListener<T> implements EventListener<T>{
	private static final Logger logger = Logger.getLogger(BaseEventListener.class);
	
	private static final String METHOD_NAME = "handleEvent".intern();
	/* (non-Javadoc)
	 * @see cn.regionsoft.one.event.EventListener#handleEvent(T)
	 */
	@Override
	public abstract Object handleEvent(T event);
	

	/* (non-Javadoc)
	 * @see cn.regionsoft.one.event.EventListener#receiveEvent(cn.regionsoft.one.event.BaseEvent)
	 */
	@Override
	public void receiveEvent(BaseEvent event) {//MethodMeta methodMeta = CommonUtil.getMethodMeta(requestNode.getControllerClass(), method);	
		try {
			T src = (T) event;
			Method[] methods = CommonUtil.getDeclaredMethods(this.getClass());
			MethodMeta methodMeta = null;
			for(Method method:methods) {
				methodMeta = CommonUtil.getMethodMeta(this.getClass(), method);
				if(method.getName().equals(METHOD_NAME) && methodMeta.getParameterTypes()[0]!=Object.class) {
					if(src.getClass() == (methodMeta.getParameterTypes()[0])) {
						handleEvent(src);
					}
					else {
						break;
					}
				}
			}	
		} catch (Exception e) {
			logger.error(e);
		}
		
	}
}
