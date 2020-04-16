package cn.regionsoft.one.event;

import cn.regionsoft.one.annotation.SlowEventListener;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.standalone.NettyHttpUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

@SlowEventListener
public class HttpFileEventListener extends BaseEventListener<HttpFileEventListener.Event> {
	public static Logger logger = Logger.getLogger(HttpFileEventListener.class);
    public HttpFileEventListener() {
    }

    @Override
    public Object handleEvent(HttpFileEventListener.Event event) {
        EventData eventData = event.getData();
        
        try {
        	//Thread.sleep(2000);
			NettyHttpUtil.processNettyMsg(eventData.getChannel(),eventData.getContextPath(),eventData.getRequest(),eventData.getMsg());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
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
    	Channel channel;
    	String contextPath;
    	HttpRequest request;
    	Object msg;
		public EventData(Channel channel, String contextPath, HttpRequest request, Object msg) {
			super();
			this.channel = channel;
			this.contextPath = contextPath;
			this.request = request;
			this.msg = msg;
		}
		
		public Channel getChannel() {
			return channel;
		}

		public void setChannel(Channel channel) {
			this.channel = channel;
		}

		public String getContextPath() {
			return contextPath;
		}
		public void setContextPath(String contextPath) {
			this.contextPath = contextPath;
		}
		public HttpRequest getRequest() {
			return request;
		}
		public void setRequest(HttpRequest request) {
			this.request = request;
		}
		public Object getMsg() {
			return msg;
		}
		public void setMsg(Object msg) {
			this.msg = msg;
		}

       
    }
}
