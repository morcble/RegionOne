package cn.regionsoft.one.event;

import java.util.concurrent.ConcurrentHashMap;
import cn.regionsoft.one.annotation.SlowEventListener;
import cn.regionsoft.one.common.Logger;

/**
 * 系统内部通用事件
 * @author fenglj
 *
 */
@SlowEventListener
public class SystemSlowEventListener extends BaseEventListener<SystemSlowEventListener.Event> {
	public static Logger logger = Logger.getLogger(SystemSlowEventListener.class);
	
	private ConcurrentHashMap<Integer,SystemEventHandler> handlers = new ConcurrentHashMap<Integer,SystemEventHandler>();
    public SystemSlowEventListener() {
    }
    
    public void addHandler(SystemEventHandler systemEventHandler) {
    	handlers.put(systemEventHandler.hashCode(), systemEventHandler);
    }
    
    @Override
    public Object handleEvent(SystemSlowEventListener.Event event) {
       EventData eventData = event.getData();
       for(SystemEventHandler systemEventHandler: handlers.values()) {
    	   try {
    		   systemEventHandler.handle(eventData);
			} catch (Exception e) {
				logger.error(e);
			}
       } 
       return null;
    }


    public static class Event extends BaseEvent<EventData> {
        private EventData data;
        
        public Event(EventData data) {
        	this.data = data;
        }
        
        @Override
        public EventData getData() {
            return data;
        }

        @Override
        public void setData(EventData data) {
            this.data = data;
        }

    }

    public static class EventData {
    	public static final int USER_CREATE = 1;
    	public static final int USER_ACCESS = 3;
    	
    	private int type;
    	private Object data;
		public EventData(int type,Object data) {
			super();
			this.type = type;
			this.data = data;
		}
		public int getType() {
			return type;
		}
		public Object getData() {
			return data;
		}
    }
}
