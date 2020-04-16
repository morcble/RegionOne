package cn.regionsoft.one.event;

import cn.regionsoft.one.annotation.SlowEventListener;
import cn.regionsoft.one.common.Logger;

@SlowEventListener
public class SampleSlowEventListener extends BaseEventListener<SampleSlowEventListener.Event> {
	public static Logger logger = Logger.getLogger(SampleSlowEventListener.class);
    public SampleSlowEventListener() {
    }

    @Override
    public Object handleEvent(SampleSlowEventListener.Event event) {
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
