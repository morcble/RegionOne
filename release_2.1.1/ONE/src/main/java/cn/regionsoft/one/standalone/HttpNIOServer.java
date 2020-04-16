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

public class HttpNIOServer implements HttpServer{
	private static final Logger logger = Logger.getLogger(HttpNIOServer.class);

	/**
	 * record all active channel
	 */
	final static Map<Integer, Channel> channelMap = new ConcurrentHashMap<Integer, Channel>();
	
	/**
	 * event mode options
	 */

	//private EventExecutor e1 = new DefaultEventExecutor();
    public void start(int port,final String contextPath,final KeyManagerFactory kmf) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(2);
        EventLoopGroup workerGroup = new NioEventLoopGroup(4);//http 服务线程  1000或者10
        try {
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                public void initChannel(SocketChannel ch) throws Exception {
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
                            })
                    .option(ChannelOption.SO_BACKLOG, 256) 
                    .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())
                    //.option(ChannelOption.SO_RCVBUF, 128)
                    //.option(ChannelOption.SO_SNDBUF, 256)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            		
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
        HttpNIOServer server = new HttpNIOServer();
        System.out.println("Http Server listening on 8844 ...");
        server.start(8844,"/MorcbleFrontend",null);
    }
}