package cn.regionsoft.one.event;

public interface EventListener<T> {

	Object handleEvent(T event);

	void receiveEvent(BaseEvent event);

}