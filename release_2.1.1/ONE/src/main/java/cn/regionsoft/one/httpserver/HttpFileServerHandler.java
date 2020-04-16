package cn.regionsoft.one.httpserver;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import javax.activation.MimetypesFileTypeMap;

import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.enums.LocaleStr;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.CharsetUtil;

public class HttpFileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest>{

    private final String context;
    private final String baseDoc;
    
    private ConcurrentHashMap<String,byte[]> resMap ;
    private ConcurrentHashMap<String,String> contentTypeMap ;
    private ConcurrentHashMap<String,Long> lastModifiedMap;
    
    public HttpFileServerHandler(String context,String baseDoc, ConcurrentHashMap<String, byte[]> resMap, ConcurrentHashMap<String, String> contentTypeMap,ConcurrentHashMap<String,Long> lastModifiedMap) {
        this.context = context;
        this.baseDoc = baseDoc;
        
        this.resMap = resMap;
        this.contentTypeMap = contentTypeMap;
        this.lastModifiedMap = lastModifiedMap;
    }
    
   
    
    private static final String T1 = "?";
    private static final String T2 = "/";
    
    private static final String LAST_MODIFIED = "last-modified";
    private static final String IF_MODIFIED_SINCE = "If-Modified-Since";
    @Override
    protected void channelRead0(ChannelHandlerContext ctx,FullHttpRequest request) throws Exception {
    	boolean keepAlive = false;
    	try {
    		if(!request.decoderResult().isSuccess()){
                sendError(ctx, HttpResponseStatus.BAD_REQUEST);
                return;
            }
            if(request.method() != HttpMethod.GET){
                sendError(ctx, HttpResponseStatus.METHOD_NOT_ALLOWED);
                return;
            }
            
            String uri = request.uri();
            int index  = uri.indexOf(T1);
            if(index!=-1) {
            	uri = uri.substring(0, index);
            }
            String path = sanitizeUri(uri);
     
            if(path == null){
                sendError(ctx, HttpResponseStatus.FORBIDDEN);
                return;
            }
            
            keepAlive = HttpUtil.isKeepAlive(request);
            
            boolean accessFirstTime = true;
            
            String lastSince = request.headers().get(IF_MODIFIED_SINCE);
            if(!CommonUtil.isEmpty(lastSince)) {
            	accessFirstTime = false;
/*            	
            	Long lastModified = lastModifiedMap.get(path);
            	if(lastModified==null) {
            		
            	}
            	else {
            		
            	}
            	
            	long lastSinceTime = sdf.parse(lastSince).getTime();*/
            	
            }

            
            
            
            byte[] cached = resMap.get(path);
            if(cached == null) {
            	synchronized(path.intern()) {
            		if(cached == null) {
            			File file = new File(path);
            	        if(file.isDirectory()) {
            	        	sendError(ctx, HttpResponseStatus.NOT_FOUND);
            	            return;
            	        }
            	        
            	        if(file.isHidden() || !file.exists()){
            	            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            	            return;
            	        }
            	        
            	        if(!file.isFile()){
            	            sendError(ctx, HttpResponseStatus.FORBIDDEN);
            	            return;
            	        }
            	        
            	        RandomAccessFile randomAccessFile = null;
            	        try{
            	            randomAccessFile = new RandomAccessFile(file, "r");
            	        }catch(FileNotFoundException fnfd){
            	            sendError(ctx, HttpResponseStatus.NOT_FOUND);
            	            return;
            	        }
            	        
            	        
            	        long fileLength = randomAccessFile.length();
            	        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            	        response.headers().set(HttpHeaderNames.CONTENT_LENGTH,fileLength);
            	        String contentType = getContentTypeHeader(file);
            	        contentTypeMap.put(path, contentType);
            	        response.headers().set(HttpHeaderNames.CONTENT_TYPE,contentType);
            	        if(HttpUtil.isKeepAlive(request)){
            	            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            	        }
            	        
            	        byte[] buffer = new byte[(int) fileLength];
            	        randomAccessFile.readFully(buffer);
            	        resMap.put(path, buffer);
            	        
            	        response.content().writeBytes(buffer);
            	        
            	        long lastModifiedDt = file.lastModified();
            	        response.headers().set(LAST_MODIFIED, new Date(lastModifiedDt));
            	        lastModifiedMap.put(path, lastModifiedDt);
            	        //response.headers().set("cache-control", "max-age=315360000");
            	        
            	        ctx.write(response);

            	        /*RandomAccessFile tmp = randomAccessFile;
            	        ChannelFuture sendFileFuture = ctx.write(new ChunkedFile(randomAccessFile, 0, fileLength, 8192), ctx.newProgressivePromise());
            	        sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
            	            
            	            @Override
            	            public void operationComplete(ChannelProgressiveFuture future)
            	                    throws Exception {
            	            	tmp.close();  
            	            }
            	            
            	            @Override
            	            public void operationProgressed(ChannelProgressiveFuture future,
            	                    long progress, long total) throws Exception {
            	                
            	            }
            	        });*/
            		}
            	}
            }
            else {//文件缓存
            	FullHttpResponse response = null;
            	
            	boolean pageChanged = false;
            	if(!CommonUtil.isEmpty(lastSince)) {
            		SimpleDateFormat sdf = CommonUtil.getSimpleDateFormat(Constants.DATE_FORMAT14, LocaleStr.en);
            		long lastSinceTime = sdf.parse(lastSince).getTime();
            		pageChanged = (lastSinceTime!=lastModifiedMap.get(path).longValue());
            	}
            	
            	if(accessFirstTime || pageChanged) {
            		response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
            		response.headers().set(HttpHeaderNames.CONTENT_LENGTH,cached.length);
        	        response.headers().set(HttpHeaderNames.CONTENT_TYPE,contentTypeMap.get(path));
        	        
        	        Long lastModifiedDt = lastModifiedMap.get(path);
        	        if(lastModifiedDt==null) {
        	        	sendError(ctx, HttpResponseStatus.NOT_FOUND);
        	            return;
        	        }
        	        else {
        	        	response.headers().set(LAST_MODIFIED, new Date(lastModifiedDt));
        	        }
        	        
        	        if(keepAlive){
        	            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        	        }
        	        
        	        response.content().writeBytes(cached);
            	}
            	else {
            		response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_MODIFIED);
            	}
    	        ctx.write(response);
            }
            
            ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
            if(!keepAlive)
                lastContentFuture.addListener(ChannelFutureListener.CLOSE);
            
    	}
    	finally {
    		if (!keepAlive) {
    			ctx.close();
			}
    	}
        
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        if(ctx.channel().isActive()) sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
    }
    
    private static final String UTF_8 = "UTF-8";
    private String sanitizeUri(String uri){
        try{
            uri = URLDecoder.decode(uri, UTF_8);
        }catch(UnsupportedEncodingException e){
            try{
                uri = URLDecoder.decode(uri, "ISO-8859-1");
            }catch(UnsupportedEncodingException e1){
                throw new Error();
            }
        }
        
        if(!uri.startsWith(context)) {
        	uri = "/error.html";
        }
        else {
        	if(!uri.startsWith(T2))
             	uri = "/error.html";
        	else {
        		uri = uri.replaceFirst(context, "");
                uri = uri.replace('/', File.separatorChar);
        	}
        }
        uri = uri.replace("//", "/");
        if(uri.equals("")||uri.equals("/"))uri = "/index.html";
        return baseDoc + uri;
    }
    
 
    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status){
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, 
                Unpooled.copiedBuffer("Failure: " + status.toString() + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html;charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    private static final String _CSS = ".css";
    private static final String _JS = ".js";
    
    
    private static final String TEXT_CSS = "text/css";
    private static final String APPLICATION_JAVASCRIPT = "application/javascript";
    
    private static String getContentTypeHeader(File file){
    	 if(file.getPath().endsWith(_CSS)) {
         	return TEXT_CSS;
         }
    	 else if(file.getPath().endsWith(_JS)) {
          	return APPLICATION_JAVASCRIPT;
         }
         else {
        	 MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
         	return mimetypesFileTypeMap.getContentType(file.getPath());
         }
    }

}