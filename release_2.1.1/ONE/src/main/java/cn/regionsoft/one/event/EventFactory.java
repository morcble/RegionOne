package cn.regionsoft.one.event;

import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;


public class EventFactory {
	private static EventFactory eventFactory = new EventFactory();
	public static EventFactory getInstance() {
		return eventFactory;
	}
	
	private final RegionEventBus eventBus;
	private EventFactory(){
		eventBus = new RegionEventBus();
	}
	
	private static Semaphore semaphore = new Semaphore(1);  

	/*
	 * add event listener with specific key.
	 */
	public void register(String listenerClassName,EventListener eventListener) {
		try {
			semaphore.acquire();
			eventBus.register(listenerClassName, eventListener);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		finally {  
			semaphore.release();  
        }  	
	}
	
	/*
	 * remove event listener with specific key.
	 */
	public void unregister(String listenerClassName) {
		try {
			semaphore.acquire();
			eventBus.unregister(listenerClassName);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		finally {
			semaphore.release();  
        } 
	}
	
	/*
	 * publish event to listeners
	 */
	public Future publishEvent(BaseEvent event) {
		return eventBus.post(event);
	}

	public void publishSyncEvent(BaseEvent event) {
		eventBus.postSync(event);
	}
}
