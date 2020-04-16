package cn.regionsoft.one.rpc.client;

import java.util.UUID;
import java.util.concurrent.Semaphore;

import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.rpc.common.RpcResponse;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
	private static final Logger logger = Logger.getLogger(RpcClientHandler.class);
	
	
	private RpcClient rpcClient;
	public RpcClientHandler(RpcClient rpcClient) {
		this.rpcClient = rpcClient;
	}

	@Override
    public void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
		Channel channel = ctx.channel();
        //String uuid = channel.attr(RpcClient.CHANNEL_UUID).get();
        channel.attr(RpcClient.CHANNEL_RESPONSE).set(response);
        channel.attr(RpcClient.CHANNEL_RESPONSE_READABLE).get().release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    	logger.error("client caught exception", cause);
    }
    
    @Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		//ctx.close();
		logger.debug("RPC Server disconnected");
		ctx.channel().attr(RpcClient.CHANNEL_UUID).set(null);
		rpcClient.connectNewChannel();
	}
    
    @Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		
		if(!ctx.channel().hasAttr(RpcClient.CHANNEL_UUID)) {
			ctx.channel().attr(RpcClient.CHANNEL_UUID).set(UUID.randomUUID().toString());
		}
		
		String uuid = ctx.channel().attr(RpcClient.CHANNEL_UUID).get();
		logger.debug("client"+getRemoteAddress(ctx)+" connected, clientId="+uuid);
		
		ctx.channel().attr(RpcClient.CHANNEL_RESPONSE_READABLE).set(new Semaphore(0));
	}
    
    public static String getRemoteAddress(ChannelHandlerContext ctx){
		return ctx.channel().remoteAddress().toString();
	}

}
