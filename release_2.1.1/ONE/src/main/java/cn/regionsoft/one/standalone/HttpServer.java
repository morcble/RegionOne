package cn.regionsoft.one.standalone;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.KeyManagerFactory;

public interface HttpServer {
	public static String ACCESS_CONTROL_ALLOW_METHODS = "POST,GET,OPTIONS";
	public static String ACCESS_CONTROL_MAX_AGE = "3600";
	//public static final String ACCESS_CONTROL_ALLOW_ORIGIN ="https://localhost:8443";
	public static String ACCESS_CONTROL_ALLOW_CREDENTIALS = "true";
	
	public static Charset REQUEST_ENCODING = io.netty.util.CharsetUtil.UTF_8;
	public static String RESPONSE_ENCODING = "UTF-8";
	public static AtomicInteger syncCount = new AtomicInteger(0);
	
	public void start(int port,final String contextPath,final KeyManagerFactory kmf) throws Exception ;
}