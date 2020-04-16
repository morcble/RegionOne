package cn.regionsoft.one.core;

import java.io.FileInputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import cn.regionsoft.one.core.SYSEnvSetup;
import cn.regionsoft.one.httpserver.SimpleHtmlServer;
import cn.regionsoft.one.properties.ConfigUtil;
import cn.regionsoft.one.standalone.StandaloneServer;

public class RegionSoftServer {
	public static void start() throws Exception {
		SYSEnvSetup.setUp();
		
		String useHttps = ConfigUtil.getProperty("https");
		if(Boolean.valueOf(useHttps)) {
			startHttpsServer();
		}
		else {
			startHttpServer();
		}
	}
	
	
	public static void startHttpServer() {
        try {
			new SimpleHtmlServer().runHttp(
					ConfigUtil.getPropertyAsInteger("frontend.port")
					, ConfigUtil.getProperty("frontend.context")
					,ConfigUtil.getProperty("frontend.html")
					);
        } catch (Exception e) {
        	System.err.println("warning : html server setup error");
		}
		try {
			StandaloneServer ss = new StandaloneServer(ConfigUtil.getProperty("backend.context",""));
			ss.runHttp();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void startHttpsServer() {
        try {
			new SimpleHtmlServer().runHttps(
					ConfigUtil.getPropertyAsInteger("frontend.port")
					, ConfigUtil.getProperty("frontend.context")
					,ConfigUtil.getProperty("frontend.html")
					,initSSLEngineKeyManager());
        } catch (Exception e) {
        	e.printStackTrace();
        	System.err.println("warning : html server setup error");
		}
		try {	
			StandaloneServer ss = new StandaloneServer(ConfigUtil.getProperty("backend.context",""));
			ss.runHttps(initSSLEngineKeyManager());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized static KeyManagerFactory initSSLEngineKeyManager() throws Exception{
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(ConfigUtil.getProperty("https.algorithm"));
		KeyStore ks = KeyStore.getInstance(ConfigUtil.getProperty("https.algorithm.type"));
		String keyStorePath = ConfigUtil.getProperty("https.keystore.path");
		String keyStorePassword = ConfigUtil.getProperty("https.keystore.password");
		ks.load(new FileInputStream(keyStorePath), keyStorePassword.toCharArray());
		String keyPassword = ConfigUtil.getProperty("https.key.password");
		kmf.init(ks, keyPassword.toCharArray());
		return kmf;
	}
}
