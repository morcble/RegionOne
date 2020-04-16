package cn.regionsoft.one.standalone;


import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.Cookie;

import cn.regionsoft.one.common.AESUtil;
import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.dispatcher.ProcessResponseWrapper;
import cn.regionsoft.one.core.dispatcher.WebRequestProcessor;
import cn.regionsoft.one.properties.ConfigUtil;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpUtil;

public class NettyHttpUtil{
	public static Logger logger = Logger.getLogger(NettyHttpUtil.class);
	
	private static int contextLength = -1;

	private static final String GET = "GET"; 
	private static final String POST = "POST"; 
	private static final String EQUAL = "="; 
	private static final String AND = "&"; 
	private static final String QUESTION = "?"; 
	private static final String ORIGIN = "Origin"; 
	private static final String REFERER = "Referer"; 
	private static final String COOKIE = "Cookie";
	private static final String COOKIE2 = "cookie";
	private static final String SEMICOLON = ";"; 
	private static final String SLASH = "/"; 
	
	private static final String HTTPONLY = "HttpOnly";
    private static final String EXPIRES = "Expires";
    private static final String PATH = "Path";
    private static final String EQUAL2 = "==";
    private static final String SERVER = "Server";
    private static final String REGION_V = "RegionSoft/V0.5.1.2";
    private static final String DATE = "Date";
    
    private static final String ACAM = "Access-Control-Allow-Methods";
   
    private static final String ACMA = "Access-Control-Max-Age";
    
    private static final String ACAH = "Access-Control-Allow-Headers";
    private static final String XRW = "x-requested-with";
    
    private static final String ACAO = "Access-Control-Allow-Origin";
    private static final String ACAOALL ="*";
    
    private static final String SET_COOKIE = "Set-Cookie";
    
    private static final String ACAC = "Access-Control-Allow-Credentials";
    
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String TEXT_PLAIN = "text/plain";
    private static final String ACEH = "Access-Control-Expose-Headers";

	
	public static void processNettyMsg(Channel channel,String contextPath,HttpRequest request,Object msg) throws Exception {
		boolean keepAlive = HttpUtil.isKeepAlive(request);
		FullHttpResponse response = null;
		try {
			response = new DefaultFullHttpResponse(HTTP_1_1, OK);
//			if(System.currentTimeMillis()>1579698284439L) {
//										  
//				return;
//			}
	    	ProcessResponseWrapper prw = processMsg(contextPath,request,msg,response,channel);
	    	
	    	if(prw.isCompleted()){
	    		return;
	    	}
	    	//byte[] src = null;
	    	String resStr = (String) prw.getResponse();
	    	
	    	if(resStr!=null) {
	    		if(prw.isEncript()) {//加密AES
	    			resStr = AESUtil.encriptCBC(resStr, ConfigUtil.getProperty(Constants.AES_KEY), ConfigUtil.getProperty(Constants.AES_IV));
	    		}
	    		
	    		response.content().writeCharSequence(resStr, Charset.forName(HttpServer.RESPONSE_ENCODING));
	    		/*
	    		src = resStr.getBytes(HttpServer.RESPONSE_ENCODING);
	        	ByteBuf bufCache = ctx.alloc().buffer(src.length).writeBytes(src);
	        	response.content().writeBytes(bufCache);
	        	bufCache.release();
	        	src = null;*/
	    	}
	    	

	        String[] responseHeader = prw.getResponseHeader();
	        if(responseHeader!=null){
				for(String tmpHeader : responseHeader){
					String tmpArray[]  = tmpHeader.split(EQUAL2);
					if(tmpArray!=null&&tmpArray.length==2){
						response.headers().set(tmpArray[0].trim(), tmpArray[1].trim());
					}
				}
			}
	        
	        //如果是加密则不能返回json头
	        if(prw.isEncript()) {
	        	response.headers().set(CONTENT_TYPE, TEXT_PLAIN);
	        	response.headers().set(Constants.ENCRYPT, true);
	        	response.headers().set(ACEH, Constants.ENCRYPT);
	        }
	        
	        response.headers().set(HttpHeaderNames.CONTENT_LENGTH,response.content().readableBytes());
	        
	        response.headers().set(SERVER, REGION_V);
	        
	        response.headers().set(ACAM, HttpServer.ACCESS_CONTROL_ALLOW_METHODS);
	        response.headers().set(ACMA, HttpServer.ACCESS_CONTROL_MAX_AGE);
	        //response.headers().set(ACAH, Constants.ENCRIPT);
	        response.headers().set(ACAO, ACAOALL);
	        
	        List<Cookie> cookieLs = prw.getHttprequestResponse().getCookieLs();
	        if(cookieLs!=null){
	        	StringBuilder sb = null;
	        	Date expireDt = null;
	        	for(Cookie tmp:cookieLs){
	        		sb = new StringBuilder();
	        		sb.append(tmp.getName());
	        		sb.append(EQUAL);
	        		sb.append(tmp.getValue());
	        		
	        		if(tmp.getMaxAge()>0){
	        			sb.append(SEMICOLON);
	        			sb.append(EXPIRES);
	            		sb.append(EQUAL);
	            		Date now = new Date();
	            		expireDt = new Date(now.getTime()+tmp.getMaxAge()*1000);
	            		sb.append(expireDt.toString());
	        		}
	        		
	        		if(!CommonUtil.isEmpty(tmp.getPath())){
	        			sb.append(SEMICOLON);
	        			sb.append(PATH);
	            		sb.append(EQUAL);
	            		sb.append(tmp.getPath());
	        		}
	        		if(tmp.isHttpOnly()){
	        			sb.append(SEMICOLON);
	        			sb.append(HTTPONLY);
	        		}
	        		response.headers().add(SET_COOKIE,sb.toString());
	        		//response.headers().set(SET_COOKIE,sb.toString());
	        		
	        		//logger.debug("set cookie="+sb.toString());
	        	}
	        }
	        
	        //TODO check 自动修改cross domain
	        String origin = request.headers().get(ORIGIN);
	        if(origin!=null) {
	        	response.headers().set(ACAO, origin);
	        }
	        else {
	        	if(request.headers()!=null) {
	        		String referer = request.headers().get(REFERER);
	        		if(referer!=null) {
	        			String[] ar = referer.split(SLASH, 4);
			    		String refererStr = ar[0]+SLASH+ar[1]+SLASH+ar[2];
			    		response.headers().set(ACAO, refererStr);
	        		}
	        	}
	        }
	        response.headers().set(ACAC, HttpServer.ACCESS_CONTROL_ALLOW_CREDENTIALS);
	        
		}
		catch(Exception e) {
			response = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
			response.content().writeCharSequence(CommonUtil.constructStackTrace(e), Charset.forName(HttpServer.RESPONSE_ENCODING));
			response.headers().set(HttpHeaderNames.CONTENT_LENGTH,response.content().readableBytes());
		}
		finally {
			try {
				if (keepAlive) {
					response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
				}

				channel.writeAndFlush(response);
				if (!keepAlive) {
					channel.close();
				}
			}
			catch(Exception e) {
				
			}
		}
    	
    }
	
	public static void main(String[] args) {
		String url = "http://localhost:18080/SilverFrontend/core/v.html?/comp/config/configlist.html";
		Pattern p =Pattern.compile("(http://|https://)[^.]*?/", Pattern.CASE_INSENSITIVE);  
        Matcher matcher = p.matcher(url);  
        matcher.find();  
        System.out.println(matcher.group());  
		
		
	}
	
	private static ProcessResponseWrapper processMsg(String contextPath,HttpRequest request, Object msg, FullHttpResponse response, Channel channel) throws Exception{
		String contentMsg = null;
		UploadMsg uploadMsg = null;
		if(msg instanceof String){
			contentMsg = (String) msg;
		}
		else if(msg instanceof UploadMsg){
			uploadMsg = (UploadMsg) msg;
		}
		 
		String reqURI = request.uri();
		if(!reqURI.startsWith(contextPath)) {
			//bad request
			throw new Exception("request path is invalid");
		}
		else {
			if(contextLength==-1){
				contextLength = contextPath.length();
			}
		}
//		if(System.currentTimeMillis()>1579698284439L) {
//			return null;
//		}
		reqURI = reqURI.substring(contextLength, reqURI.length());

		Map<String, String[]> requestMap = new HashMap<String, String[]>();
		int hasPara = reqURI.indexOf(QUESTION);
		String paraStr = null;
		if(hasPara!=-1){
			paraStr = reqURI.substring(hasPara+1);
			reqURI = reqURI.substring(0,hasPara);
			if(!CommonUtil.isEmpty(paraStr)){
				String[] paraArray = paraStr.split(AND);
				String[] paraPair = null;
				for(String tmp:paraArray){
					paraPair = tmp.split(EQUAL);
					if(paraPair.length!=2)continue;
					requestMap.put(paraPair[0], new String[]{URLDecoder.decode(paraPair[1], HttpServer.REQUEST_ENCODING.name())});
				}
				paraStr = null;
			}
		}
		
		String method = null;
		
		if(request.method() == HttpMethod.POST){
			method = POST;
			paraStr = contentMsg;	
		}
		else if(request.method() == HttpMethod.GET){
			method = GET;
		}
		else {
			method = POST;
		}
		
		if(!CommonUtil.isEmpty(paraStr)){
			String[] paraArray = paraStr.split(AND);
			String[] paraPair = null;
			for(String tmp:paraArray){
				paraPair = tmp.split(EQUAL);
				if(paraPair.length!=2)continue;
				requestMap.put(paraPair[0], new String[]{URLDecoder.decode(paraPair[1], HttpServer.REQUEST_ENCODING.name())});
			}
		}
		
			
		//resolve header
		Map<String,String> headerInfo = new HashMap<String,String>();
		HttpHeaders headers = request.headers();
		Iterator<Entry<String,String>> iterator = headers.iteratorAsString();
		Entry<String,String> tmp = null;
		while(iterator.hasNext()){
			tmp = iterator.next();
			headerInfo.put(tmp.getKey(), tmp.getValue());
		}
		
		
		//resolve cookies
		Map<String,String> cookiesMap = new HashMap<String,String>();	
		String cookieStr = headerInfo.get(COOKIE);
		
		
		if(cookieStr==null)
			cookieStr = headerInfo.get(COOKIE2);//某些浏览器不标准
		
		if(cookieStr!=null) {
			String[] cookieArray = cookieStr.split(SEMICOLON);
			for(String tmpCookie:cookieArray){
				String[] tmpArray = tmpCookie.split(EQUAL);
				cookiesMap.put(tmpArray[0].trim(),tmpArray[1].trim());
			}
		}
		
		
		RegionHttpRequest httprequest = new RegionHttpRequest();
		httprequest.setHeaderInfo(headerInfo);
		httprequest.setCookiesMap(cookiesMap);
		httprequest.setRequestMap(requestMap);
		httprequest.setMethod(method);
		// HttpServletRequest httprequest.setContentType(contentType);
		if(uploadMsg!=null){
			httprequest.setUploadMsg(uploadMsg);
		}
		
		RegionHttpResponse httprequestResponse = new RegionHttpResponse();
		httprequestResponse.setFullHttpResponse(response);
		httprequestResponse.setRequest(request); 
		httprequestResponse.setChannelHandlerContext(channel);
		ProcessResponseWrapper prw;
		try {
			prw = WebRequestProcessor.process(reqURI,method,requestMap,headerInfo,cookiesMap,httprequest,httprequestResponse);
			prw.setCompleted(httprequestResponse.isCompleted());
			prw.setHttprequestResponse(httprequestResponse);
			return prw;
		} catch (Exception e) {
			throw e;
		}
	}
    
    
}
