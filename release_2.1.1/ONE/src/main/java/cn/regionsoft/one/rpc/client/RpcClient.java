package cn.regionsoft.one.rpc.client;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.rpc.common.RpcDecoder;
import cn.regionsoft.one.rpc.common.RpcEncoder;
import cn.regionsoft.one.rpc.common.RpcRequest;
import cn.regionsoft.one.rpc.common.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;
import io.netty.util.concurrent.DefaultThreadFactory;


public class RpcClient{
    private static final Logger logger = Logger.getLogger(RpcClient.class);
    private static EventLoopGroup group = null;
    
    static {
    	if(Epoll.isAvailable()) {
    		group = new EpollEventLoopGroup(0, new DefaultThreadFactory(EpollEventLoopGroup.class));
    	}
    	else {
    		group = new NioEventLoopGroup(0, new DefaultThreadFactory(NioEventLoopGroup.class));
    	}
    }
    
    public static AttributeKey<String> CHANNEL_UUID = AttributeKey.valueOf("channel_uuid");
    public static AttributeKey<RpcResponse> CHANNEL_RESPONSE = AttributeKey.valueOf("channel_response");
    public static AttributeKey<Semaphore> CHANNEL_RESPONSE_READABLE = AttributeKey.valueOf("channel_semaphore");
    private String host;
    private int port;

    private Bootstrap bootstrap = new Bootstrap();
    private String serverAddr = null;
    
    
    private int activeChannelCount = Runtime.getRuntime().availableProcessors()*2+1;
    private BlockingQueue<Channel> activeChannels = new LinkedBlockingQueue<Channel>();


	public RpcClient(String host, int port) {
        this.host = host;
        this.port = port;
        
        serverAddr = host+":" +port;
        
        init();
    }
    public String getServerAddr() {
		return serverAddr;
	}
    
    static {
		ResourceLeakDetector.setLevel(Level.DISABLED);
	}
    
    private void init(){
    	try{
    		if(Epoll.isAvailable()) {
    			bootstrap.group(group).channel(EpollSocketChannel.class)
	    	        .handler(new ChannelInitializer<EpollSocketChannel>() {
	    	            @Override
	    	            public void initChannel(EpollSocketChannel channel) throws Exception {
	    	              /*  channel.pipeline().addLast(new ReadTimeoutHandler(3000));
			            	channel.pipeline().addLast(new WriteTimeoutHandler(3000));*/
			            	channel.pipeline().addLast(new RpcEncoder(RpcRequest.class));
			            	channel.pipeline().addLast(new RpcDecoder(RpcResponse.class));
			            	channel.pipeline().addLast(new RpcClientHandler(RpcClient.this));
	    	            }
	    	        });
	    		bootstrap.option(EpollChannelOption.SO_REUSEPORT, true);
    		}
    		else {
    			bootstrap.group(group).channel(NioSocketChannel.class)
	    	        .handler(new ChannelInitializer<NioSocketChannel>() {
	    	            @Override
	    	            public void initChannel(NioSocketChannel channel) throws Exception {
	    	              /*  channel.pipeline().addLast(new ReadTimeoutHandler(3000));
			            	channel.pipeline().addLast(new WriteTimeoutHandler(3000));*/
			            	channel.pipeline().addLast(new RpcEncoder(RpcRequest.class));
			            	channel.pipeline().addLast(new RpcDecoder(RpcResponse.class));
			            	channel.pipeline().addLast(new RpcClientHandler(RpcClient.this));
	    	            }
	    	        });
    		}
    		
    		bootstrap.option(ChannelOption.SO_REUSEADDR, true);
    		bootstrap.option(ChannelOption.SO_RCVBUF, 256);
    		bootstrap.option(ChannelOption.SO_SNDBUF, 256);
    		bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK,new WriteBufferWaterMark(32 * 1024, 64 * 1024));
    		
    		
    		bootstrap.option(ChannelOption.SO_BACKLOG, 256) ;
    		bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
    		
    		for(int i = 0 ; i <activeChannelCount;i++) {
    			Channel channel = bootstrap.connect(host, port).sync().channel();
    			activeChannels.add(channel);
    		}
    	}
    	catch(Exception e){
    		logger.error(e);
    	}
    	finally {
    		Runtime.getRuntime().addShutdownHook(
    	        	new Thread(() -> {
    	        		Iterator<Channel> iterator  = activeChannels.iterator();
    	        		while(iterator.hasNext())
    	        			iterator.next().close();
    	        		group.shutdownGracefully();
    	        	}
    	    ));
    	}
		
    }

    
	public void connectNewChannel() {
		boolean connected = false;
		while(!connected) {
			logger.debug("reconnecting...");
			try {
				Channel channel = bootstrap.connect(host, port).sync().channel();
				activeChannels.add(channel);
			}
			catch(Exception e) {
				logger.debug("connect failed...");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
    
    public Channel send(RpcRequest request) throws Exception {	
    	Channel channel = activeChannels.take();
    	String channelUUID = channel.attr(CHANNEL_UUID).get();
    	while(channelUUID==null) {
    		channel = activeChannels.take();
    		channelUUID = channel.attr(CHANNEL_UUID).get();
    	}
   	 	channel.writeAndFlush(request);
        return channel;
    }

    public RpcResponse getResponse(Channel channel) throws InterruptedException {
    	channel.attr(CHANNEL_RESPONSE_READABLE).get().acquire();
    	RpcResponse rpcResponse = channel.attr(CHANNEL_RESPONSE).get();
    	activeChannels.add(channel);
		return rpcResponse;
	}


    public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
	
}
