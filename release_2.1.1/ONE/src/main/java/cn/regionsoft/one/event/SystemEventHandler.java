package cn.regionsoft.one.event;

import cn.regionsoft.one.event.SystemSlowEventListener.EventData;

public interface SystemEventHandler {
	public void handle(EventData eventData) throws Exception;
}
