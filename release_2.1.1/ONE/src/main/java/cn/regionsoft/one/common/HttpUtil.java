package cn.regionsoft.one.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import org.apache.commons.io.IOUtils;

  
public class HttpUtil {  
	private static final Logger logger = Logger.getLogger(HttpUtil.class);
	
	public static final String CONTENT_JSON = "application/json;charset=UTF-8";
	public static final String CONTENT_URLENCODED = "application/x-www-form-urlencoded";
	
    public static String post(String httpsUrl, String method,String content,String contentType) throws Exception {
    	return requestWithTimeOut(httpsUrl,method,content,contentType,-1);
    }  
    
    static {
    	try {
    		SSLContext sslcontext = SSLContext.getInstance("SSL", "SunJSSE");//第一个参数为协议,第二个参数为提供者(可以缺省)
        	TrustManager[] tm = {new MyX509TrustManager()};
        	sslcontext.init(null, tm, new SecureRandom());
        	HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
        		public boolean verify(String s, SSLSession sslsession) {
        			//System.out.println("WARNING: Hostname is not matched for cert.");
        				return true;
        		}
        	};
        	HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
	        	HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
	    }
	    catch(Exception e) {
	    	e.printStackTrace();
	    }
    	
    }
    
    public static String getWithTimeOut(String httpsUrl, String content,int timeout) throws Exception {
    	return requestWithTimeOut(httpsUrl,GET,content,null,-1);
    }
    
    public static String post(String httpsUrl,String content) throws Exception {
    	return requestWithTimeOut(httpsUrl,POST,content,null,-1);
    }
    
    public static String requestWithTimeOut(String httpsUrl, String method,String content,int timeout) throws Exception {
    	return requestWithTimeOut(httpsUrl,method,content,null,-1);
    }
    
    public static final String CONTENT_LENGTH = "Content-Length";
    public static final String ZERO = "0";
    public static final String UTF_8 = "UTF-8";
    public static final String GET = "GET";
    public static final String POST = "POST";
    public static String requestWithTimeOut(String httpsUrl, String method,String content,String contentType,int timeout) throws Exception {
        HttpURLConnection urlCon = null;  
        try {  
            urlCon = (HttpURLConnection) (new URL(httpsUrl)).openConnection();  
            urlCon.setDoInput(true);  
            urlCon.setDoOutput(true);  
            if(timeout>100){
            	urlCon.setConnectTimeout(timeout);
                urlCon.setReadTimeout(timeout);
            }
            else{
            	urlCon.setConnectTimeout(100000);
            	urlCon.setReadTimeout(100000);
            }
            urlCon.setRequestMethod(method);  
            if(content!=null)
            	urlCon.setRequestProperty(CONTENT_LENGTH,String.valueOf(content.getBytes().length));  
            else
            	urlCon.setRequestProperty(CONTENT_LENGTH,ZERO);  
            urlCon.setUseCaches(false);  
            //urlCon.setRequestProperty("accept", "*/*");
            //urlCon.setRequestProperty("connection", "Keep-Alive");
            //urlCon.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            //urlCon.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36");
            
           /* if(jSessionId!=null){
            	urlCon.setRequestProperty("Cookie", jSessionId);
            }*/
            
            /*urlCon.setRequestProperty("Content-Type","application/x-www-form-urlencoded");*/
            
            if(contentType!=null&&!contentType.equals("")){
            	 urlCon.setRequestProperty("Content-Type",contentType);
            }
            //urlCon.setRequestProperty("connection", "Keep-Alive");
            urlCon.connect();
            
            if(content!=null){
            	DataOutputStream out = new DataOutputStream(urlCon.getOutputStream());
            	out.writeBytes(content);
            	out.flush();
            	out.close(); 
            }
            
			StringBuilder str = new StringBuilder();
			InputStreamReader in = null;
			BufferedReader reader = null;
			try{
				in = new InputStreamReader(urlCon.getInputStream(),UTF_8);
				reader = new BufferedReader(in);
				String line = null;
				while((line=reader.readLine()) != null){
					str.append(line);
				}
			}
			catch(Exception e){
				throw e; 
			}
			finally{
				if(reader!=null)reader.close();
				if(in!=null)in.close();
			}
			return str.toString();
  
        } catch (Exception e) {  
            throw e; 
        }  
    }
   
    
	public static InputStream downloadFile(String websiteDomain, String relativePath, String destFolder) throws IOException {
		if (!relativePath.startsWith("/"))
			relativePath = "/" + relativePath;

		int index = relativePath.lastIndexOf("/");
		String relativeFolder = relativePath.substring(0, index);
		String fileName = relativePath.substring(index + 1);

		if (destFolder.endsWith("/"))
			destFolder = destFolder.substring(0, destFolder.length() - 1);
		File folder = new File(destFolder + relativeFolder);
		if (!folder.exists())
			folder.mkdirs();
		String destFileName = folder.getAbsolutePath() + Constants.SYSTEM_SEPERATOR + fileName;

		InputStream inputStream = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			//logger.debug("starting download file:"+websiteDomain + relativePath);
			HttpURLConnection conn = (HttpURLConnection) new URL(websiteDomain + relativePath).openConnection();
			conn.setReadTimeout(500000);
			conn.setConnectTimeout(500000);
			conn.setRequestMethod("GET");

			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				inputStream = conn.getInputStream();
				bis = new BufferedInputStream(inputStream);
				bos = new BufferedOutputStream(new FileOutputStream(new File(destFileName)));
				byte[] buffer = new byte[1024];
				int len;
				while ((len = bis.read(buffer)) != -1) {
					bos.write(buffer, 0, len);
					bos.flush();
				}
			}
		} catch (IOException e) {
			throw e;
		} finally {
			IOUtils.closeQuietly(bos);
			IOUtils.closeQuietly(bis);
			IOUtils.closeQuietly(inputStream);
		}
		
		//logger.debug("finish download file:"+websiteDomain + relativePath);
		return null;
	}
  
  
    /** 
     * 测试方法. 
     * @param args 
     * @throws Exception 
     */  
    public static void main(String[] args) throws Exception {  
    	String url = "https://localhost:8080/region/wechat/auth"; 
    	long time = System.currentTimeMillis();
    	for(int i = 0 ; i<10000 ; i++) {
    		HttpUtil.post(url,"");
    	}
    	System.out.println(System.currentTimeMillis()-time);
    	String result = HttpUtil.post(url,"");
    	System.out.println(result);
    
    }  
}  
