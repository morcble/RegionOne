package cn.regionsoft.one.standalone;

import javax.net.ssl.KeyManagerFactory;
import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.SYSEnvSetup;
import cn.regionsoft.one.properties.ConfigUtil;

import io.netty.channel.epoll.Epoll;

public class StandaloneServer {
	public static String contextPath;
	
	public StandaloneServer(String contextPath){
		StandaloneServer.contextPath = contextPath+"/region";
	}
	public void runHttp(){
		try {
			SYSEnvSetup.setUp();
			String portStr = ConfigUtil.getProperty("backend.port");
			if(CommonUtil.isEmpty(portStr)) {
				portStr = "8080";
			}
			
			HttpServer server = null;
			if(Epoll.isAvailable()) {
				server = new HttpEpollServer();
			}
			else {
				server = new HttpNIOServer();
			}
			
	        server.start(Integer.parseInt(portStr),StandaloneServer.contextPath,null);
	        
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
/*	public static void main(String[] args){
		StandaloneServer ss = new StandaloneServer(8088,"/MorcbleFrontend");
		ss.runHttp();
	}
	*/

	public void runHttps(KeyManagerFactory keyManagerFactory) {
		try {
			SYSEnvSetup.setUp();
			String portStr = ConfigUtil.getProperty("backend.port");
			if(CommonUtil.isEmpty(portStr)) {
				portStr = "8080";
			}
			
			HttpServer server = null;
			if(Epoll.isAvailable()) {
				server = new HttpEpollServer();
			}
			else {
				server = new HttpNIOServer();
			}
	        server.start(Integer.parseInt(portStr),StandaloneServer.contextPath,keyManagerFactory);
	        
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void main(String[] args) {
		StandaloneServer a = new StandaloneServer("123");
	}
}
