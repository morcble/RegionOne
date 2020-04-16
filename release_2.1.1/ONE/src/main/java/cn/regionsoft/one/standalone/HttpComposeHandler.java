package cn.regionsoft.one.standalone;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.ReferenceCountUtil;

public class HttpComposeHandler extends ChannelInboundHandlerAdapter {
    public HttpComposeHandler(){
    	bufCache = PooledByteBufAllocator.DEFAULT.buffer();
    }
    private ByteBuf bufCache = null;
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
    	if (msg instanceof HttpRequest) {
    		ctx.fireChannelRead(msg);
    	}
    	else if (msg instanceof HttpContent) {
    		if(bufCache.capacity()>5048576){
    			bufCache = bufCache.clear();		
    			ReferenceCountUtil.release(msg);
    			
    			FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
    			response.content().writeCharSequence("http body is too large ,max body size is 5 MB", Charset.forName(HttpServer.RESPONSE_ENCODING));
    			ctx.channel().writeAndFlush(response);
    			return;
    		}
    		
        	HttpContent content = (HttpContent) msg; 
        	ByteBuf buf = content.content();

        	int spaceNeed = buf.capacity()-bufCache.writableBytes();
        	if(spaceNeed>0){
        		bufCache.capacity(bufCache.capacity()+spaceNeed);
        	}
    		bufCache.writeBytes(buf, 0, buf.capacity());
    		
            //buf.release();
        	if(content instanceof LastHttpContent){  
        		String contentMsg = bufCache.toString(HttpServer.REQUEST_ENCODING);
        		bufCache = bufCache.clear();
        		ctx.fireChannelRead(contentMsg);
            } 
        	
        	ReferenceCountUtil.release(content);
        }
    	else{
    		ctx.fireChannelRead(msg);
    	}
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	bufCache.release();
        ctx.fireChannelInactive();
    }
}
