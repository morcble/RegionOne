package cn.regionsoft.one.event;

import java.util.concurrent.Future;

public abstract class BaseEvent<T>{
	public abstract T getData();

	public abstract void setData(T data);
	
	/**
	 * 异步事件
	 * @return
	 */
	public Future publish() {
		return EventFactory.getInstance().publishEvent(this);
	}
	
	/**
	 * 同步事件
	 */
	public void publishSync() {
		EventFactory.getInstance().publishSyncEvent(this);
	}
}
