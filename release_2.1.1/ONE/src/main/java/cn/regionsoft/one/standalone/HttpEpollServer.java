package cn.regionsoft.one.standalone;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import cn.regionsoft.one.common.Logger;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.ResourceLeakDetector.Level; 

public class HttpEpollServer implements HttpServer{
	private static final Logger logger = Logger.getLogger(HttpEpollServer.class);
	
	static {
		ResourceLeakDetector.setLevel(Level.DISABLED);
	}
	/**
	 * record all active channel
	 */
	final static Map<Integer, Channel> channelMap = new ConcurrentHashMap<Integer, Channel>();
	
	/**
	 * event mode options
	 */

	//private EventExecutor e1 = new DefaultEventExecutor();
    public void start(int port,final String contextPath,final KeyManagerFactory kmf) throws Exception {
        EventLoopGroup bossGroup = new EpollEventLoopGroup(1);
        EventLoopGroup workerGroup = new EpollEventLoopGroup();//http 服务线程  1000或者10
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(EpollServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<EpollSocketChannel>() {
                                @Override
                                public void initChannel(EpollSocketChannel ch) throws Exception {
                                	ChannelPipeline pipeline = ch.pipeline();
                                	if(kmf!=null){
                                		SSLContext sslcontext = SSLContext.getInstance("TLS");                          		
                                		sslcontext.init(kmf.getKeyManagers(), null, null);
                                		SSLEngine sslEngine = sslcontext.createSSLEngine();
                                		sslEngine.setUseClientMode(false);
                                		sslEngine.setNeedClientAuth(false);
                                		pipeline.addLast(new SslHandler(sslEngine));
                                	}
                                	
                                	pipeline.addLast(new ReadTimeoutHandler(60,TimeUnit.SECONDS));
                                	pipeline.addLast(new WriteTimeoutHandler(60,TimeUnit.SECONDS));
                                	pipeline.addLast(new HttpResponseEncoder());
                                 	pipeline.addLast(new HttpRequestDecoder());
                                 	if(kmf!=null){
                                 		pipeline.addLast(new ChunkedWriteHandler()); //for https write file
                                 	}
                                 
                                    pipeline.addLast(new HttpFileUploadHandler()); 
                                    pipeline.addLast(new HttpComposeHandler()); 
                                	//pipeline.addLast(new IdleStateHandler(0,0,300),new HttpServerRequestHandler(contextPath));
                                	pipeline.addLast(new HttpServerRequestHandler(contextPath));
                                }
                            });
            b.option(EpollChannelOption.SO_REUSEPORT, true);
            b.option(ChannelOption.SO_BACKLOG, 1024 * 8);
			b.option(ChannelOption.SO_REUSEADDR, true);
			b.option(ChannelOption.SO_RCVBUF, 256 * 1024);
			b.childOption(ChannelOption.SO_REUSEADDR, true);

			b.childOption(ChannelOption.SO_REUSEADDR, true);
			b.childOption(ChannelOption.SO_RCVBUF, 256 * 1024);
			b.childOption(ChannelOption.SO_SNDBUF, 256 * 1024);
			b.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1024 * 1024, 2048 * 1024));
            		
            ChannelFuture f = b.bind(port).sync();
            logger.debug("Regionsoft Server listening on "+port+" ...");
            //f.channel().closeFuture().sync();
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



    public static void main(String[] args) throws Exception {
        HttpEpollServer server = new HttpEpollServer();
        System.out.println("Http Server listening on 8844 ...");
        server.start(8844,"/MorcbleFrontend",null);
    }
}