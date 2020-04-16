package cn.regionsoft.one.rpc.server;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.properties.ConfigUtil;
import cn.regionsoft.one.rpc.client.MicrosvcManager;
import cn.regionsoft.one.rpc.common.RpcDecoder;
import cn.regionsoft.one.rpc.common.RpcEncoder;
import cn.regionsoft.one.rpc.common.RpcRequest;
import cn.regionsoft.one.rpc.common.RpcResponse;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level;

/**
 * @author morcble
 */
public class RPCServer {
	private static final Logger logger = Logger.getLogger(RPCServer.class);
	
	private static RPCServer instance;
	private RPCServer() {}

	public static RPCServer getInstance(){
		if(instance==null) {
			synchronized(RPCServer.class){
				if(instance==null) {
					instance = new RPCServer();
				}
			}
		}
		return instance;
	}
	
	static {
		ResourceLeakDetector.setLevel(Level.DISABLED);
	}
	
	private static EventLoopGroup bossGroup = null;
	private static EventLoopGroup workerGroup = null;
	static {
		if(Epoll.isAvailable()) {
       	 	bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup();
		}
		else {
			bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
		}
	}
	
	public void startSvc(String serverAddress,MicrosvcManager microsvcManager) {
		Map<String, Object> handlerMap = microsvcManager.getRpcHandlerMap();
		String[] array = serverAddress.split(":");
        String host = array[0];
        int port = Integer.parseInt(array[1]);
		
		Iterator<String> iterator = handlerMap.keySet().iterator();
        while(iterator.hasNext()) {
        	 microsvcManager.getZooKeeperManager().regService(iterator.next(),serverAddress);
        }
       
        try {
            ServerBootstrap b = new ServerBootstrap();
            
            if(Epoll.isAvailable()) {
            	 b.group(bossGroup, workerGroup).channel(EpollServerSocketChannel.class)
		                 .childHandler(new ChannelInitializer<EpollSocketChannel>() {
		                             @Override
		                             public void initChannel(EpollSocketChannel ch) throws Exception {
		//                             	ch.pipeline().addLast(new ReadTimeoutHandler(3000));
		//                             	ch.pipeline().addLast(new WriteTimeoutHandler(3000));
		                             	ch.pipeline().addLast(new RpcDecoder(RpcRequest.class));
		                             	ch.pipeline().addLast(new RpcEncoder(RpcResponse.class));
		                             	ch.pipeline().addLast(new RpcHandler(handlerMap));
		                             }
		                         });
		         b.option(EpollChannelOption.SO_REUSEPORT, true);
            }
            else {
	           	 b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
			             .childHandler(new ChannelInitializer<NioSocketChannel>() {
			                         @Override
			                         public void initChannel(NioSocketChannel ch) throws Exception {
		//                             	ch.pipeline().addLast(new ReadTimeoutHandler(3000));
		//                             	ch.pipeline().addLast(new WriteTimeoutHandler(3000));
			                         	ch.pipeline().addLast(new RpcDecoder(RpcRequest.class));
			                         	ch.pipeline().addLast(new RpcEncoder(RpcResponse.class));
			                         	ch.pipeline().addLast(new RpcHandler(handlerMap));
			                         }
			                     });
            }
            
           
            b.option(ChannelOption.SO_BACKLOG, 1024 * 8);
			b.option(ChannelOption.SO_REUSEADDR, true);
			b.option(ChannelOption.SO_RCVBUF, 256 * 1024);
			b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    		b.option(ChannelOption.SO_KEEPALIVE, true);
    		
			b.childOption(ChannelOption.SO_REUSEADDR, true);
			b.childOption(ChannelOption.SO_RCVBUF, 256 * 1024);
			b.childOption(ChannelOption.SO_SNDBUF, 256 * 1024);
			b.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1024 * 1024, 2048 * 1024));
    		b.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
    		b.childOption(ChannelOption.SO_KEEPALIVE, true);
            		
            b.bind(port).sync();
            logger.debug("RPC Server listening on "+port+" ...");
        } 
        catch(InterruptedException e) {
        	logger.error(e);
        }
        finally {
        	 Runtime.getRuntime().addShutdownHook(
        	        	new Thread(() -> {
        	        		bossGroup.shutdownGracefully();
        	        		workerGroup.shutdownGracefully();
        	        	}
        	        ));
        }
	}
}
