package cn.regionsoft.one.event;

import cn.regionsoft.one.annotation.QuickEventListener;
import cn.regionsoft.one.common.Logger;

@QuickEventListener
public class SampleQuickEventListener extends BaseEventListener<SampleQuickEventListener.Event> {
	public static Logger logger = Logger.getLogger(SampleQuickEventListener.class);
    public SampleQuickEventListener() {
    }

    @Override
    public Object handleEvent(SampleQuickEventListener.Event event) {
        EventData eventData = event.getData();
        return null;
    }


    public static class Event extends BaseEvent<EventData> {
        private EventData data;

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
       

        public EventData() {

        }

       
    }
}
