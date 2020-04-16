package cn.regionsoft.one.httpserver;

import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.standalone.HttpContentCompressorHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class SimpleHtmlServer {
	//path- file content
	private ConcurrentHashMap<String,byte[]> resMap = new ConcurrentHashMap<String,byte[]>();
	//path- file type
	private ConcurrentHashMap<String,String> contentTypeMap = new ConcurrentHashMap<String,String>();
	//path- last modified
	private ConcurrentHashMap<String,Long> lastModifiedMap = new ConcurrentHashMap<String,Long>();
    
    public void runHttp(final int port, final String context,final String baseDoc)throws Exception{
    	if(CommonUtil.isEmpty(context)||CommonUtil.isEmpty(baseDoc))return;
    	
    	System.out.println("前端服务地址： " + "http://"+InetAddress.getLocalHost().getHostAddress()+":" + port + context+"/index.html");
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try{
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                	ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast(new HttpRequestDecoder());
                    pipeline.addLast(new HttpObjectAggregator(65536));
                    pipeline.addLast(new HttpResponseEncoder());
                    pipeline.addLast(new ChunkedWriteHandler());
                    pipeline.addLast(new HttpContentCompressor());
                    pipeline.addLast(new HttpFileServerHandler(context,baseDoc,resMap,contentTypeMap,lastModifiedMap));
                }
            })
            .option(ChannelOption.SO_BACKLOG, 256) 
            .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())
            //.option(ChannelOption.SO_RCVBUF, 128)
            //.option(ChannelOption.SO_SNDBUF, 256)
            .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .childOption(ChannelOption.SO_KEEPALIVE, true);
            
            FileChangeListener fileChangeListener = new FileChangeListener(baseDoc,resMap,contentTypeMap,lastModifiedMap);
            fileChangeListener.start();
            
            ChannelFuture f = b.bind(port).sync();
            
        }finally{
        	Runtime.getRuntime().addShutdownHook(
                	new Thread(() -> {
                		bossGroup.shutdownGracefully();
                		workerGroup.shutdownGracefully();
                	}
                ));
        }
    }
    
    
    
    public void runHttps(final int port, final String context,final String baseDoc,final KeyManagerFactory kmf)throws Exception{
    	if(CommonUtil.isEmpty(context)||CommonUtil.isEmpty(baseDoc))return;
    	System.out.println("Html服务地址： " + "https://"+InetAddress.getLocalHost().getHostAddress()+":" + port + context+"/index.html");
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        
        try{
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                	ChannelPipeline pipeline = ch.pipeline();
                	if(kmf!=null){
                		SSLContext sslcontext = SSLContext.getInstance("TLS");                          		
                		sslcontext.init(kmf.getKeyManagers(), null, null);
                		SSLEngine sslEngine = sslcontext.createSSLEngine();
                		sslEngine.setUseClientMode(false);
                		sslEngine.setNeedClientAuth(false);
                		pipeline.addLast(new SslHandler(sslEngine));
                	}
                	
                    pipeline.addLast(new HttpRequestDecoder());
                    pipeline.addLast(new HttpObjectAggregator(65536));
                    pipeline.addLast(new HttpResponseEncoder());
                    pipeline.addLast(new ChunkedWriteHandler());
                    pipeline.addLast(new HttpFileServerHandler(context,baseDoc,resMap,contentTypeMap,lastModifiedMap));
                }
            })
            .option(ChannelOption.SO_BACKLOG, 256) 
            .option(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator())
            //.option(ChannelOption.SO_RCVBUF, 128)
            //.option(ChannelOption.SO_SNDBUF, 256)
            .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .childOption(ChannelOption.SO_KEEPALIVE, true);
            
            ChannelFuture f = b.bind(port).sync();
            
        }finally{
        	Runtime.getRuntime().addShutdownHook(
                	new Thread(() -> {
                		bossGroup.shutdownGracefully();
                		workerGroup.shutdownGracefully();
                	}
                ));
        }
    }
    
    
    
    public static void main(String[] args)throws Exception {
    	String htmlContext = "/MorcbleFrontend";
        String baseDoc = "/home/fenglj/devspace/myprojects/Morcble/MorcbleFrontend/src/main/webapp";
        new SimpleHtmlServer().runHttp(8888, htmlContext,baseDoc);
    }
}