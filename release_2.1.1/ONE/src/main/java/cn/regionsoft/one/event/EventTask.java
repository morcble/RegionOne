package cn.regionsoft.one.event;

public class EventTask implements Runnable{
	private BaseEvent event;
	private EventListener eventListener;
	
	public EventTask(BaseEvent event, EventListener eventListener) {
		super();
		this.event = event;
		this.eventListener = eventListener;
	}

	@Override
	public void run() {
		try {
			eventListener.handleEvent(this.event);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
	}

}
