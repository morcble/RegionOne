package cn.regionsoft.one.standalone;

import java.util.concurrent.Future;

import cn.regionsoft.one.event.HttpFileEventListener;
import cn.regionsoft.one.event.HttpMsgEventListener;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

public class HttpServerRequestHandler extends ChannelInboundHandlerAdapter {
	private static AttributeKey<Integer> CHANNEL_UUID = AttributeKey.valueOf("channelUuid");
	
    private final String currContextPath;
    private HttpRequest currRequest;
    public HttpServerRequestHandler(final String contextPath){
    	this.currContextPath = contextPath;
    }
    
  /*  @Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		String socketString = ctx.channel().remoteAddress().toString();
		if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
            	System.out.println("Client: "+socketString+" READER_IDLE 读超时");
                ctx.disconnect();
            } else if (event.state() == IdleState.WRITER_IDLE) {
            	System.out.println("Client: "+socketString+" WRITER_IDLE 写超时");
                ctx.disconnect();
            } else if (event.state() == IdleState.ALL_IDLE) {
            	System.out.println("Client: "+socketString+" ALL_IDLE 总超时");
                ctx.disconnect();
            }
        }
	}*/
    
//    @Override
//	public void channelActive(ChannelHandlerContext ctx) throws Exception {
//		super.channelActive(ctx);
//		
//	/*	Integer uid = ctx.channel().toString().hashCode();
//		ctx.channel().attr(CHANNEL_UUID).set(uid);
//		HttpServer.channelMap.put(uid, ctx.channel());	*/
//    }
//    
//    @Override
//	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//		ctx.close();
//	}
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
    	if (msg instanceof String) {
    		int syncCount = HttpServer.syncCount.get();
    		if(syncCount>300) {//大于300 开启备用异步线程池
    			//System.out.println(11111);
    			HttpMsgEventListener.Event event = new HttpMsgEventListener.Event();
        		HttpMsgEventListener.EventData eventData = new HttpMsgEventListener.EventData(ctx.channel(),currContextPath,currRequest,msg);
        		event.setData(eventData);
        		event.publish();
    		}
    		else {
    			HttpServer.syncCount.incrementAndGet();
    			NettyHttpUtil.processNettyMsg(ctx.channel(),currContextPath,currRequest,msg);
    			syncCount = HttpServer.syncCount.decrementAndGet();
    		}				
        }
    	else if (msg instanceof HttpRequest) {
    		currRequest = (HttpRequest) msg;
        }
    	else if (msg instanceof UploadMsg) {
    		currRequest = ((UploadMsg) msg).getCurrRequest();
    		HttpFileEventListener.Event event = new HttpFileEventListener.Event();
    		HttpFileEventListener.EventData eventData = new HttpFileEventListener.EventData(ctx.channel(),currContextPath,currRequest,msg);
    		event.setData(eventData);
    		Future future = event.publish();
    		
    		//currRequest = ((UploadMsg) msg).getCurrRequest();//?? TODO 改成异步
    		//NettyHttpUtil.processNettyMsg(ctx.channel(),currContextPath,currRequest,msg,true);	
        }
    	else {
    		ReferenceCountUtil.release(msg);
    	}
    }

//    @Override
//    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
//        ctx.flush();
//    }

//    @Override
//    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//		ctx.close();
//    }
    
}
