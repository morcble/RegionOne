package cn.regionsoft.one.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PakoUtils {
	public static String compress(String str) {
		try {
			str = URLEncoder.encode(str,"UTF-8");
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			GZIPOutputStream gzip = new GZIPOutputStream(out);
			gzip.write(str.getBytes());
			gzip.close();
			return new String(Base64.getEncoder().encode(out.toByteArray()), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


    public static String uncompress(String compressedStr) {  
      byte[] bytes = Base64.getDecoder().decode(compressedStr);
      ByteArrayOutputStream out = null;   
      ByteArrayInputStream in = null; 
      try{
    	  out = new ByteArrayOutputStream();   
          in = new ByteArrayInputStream(bytes);
    	  GZIPInputStream gunzip = new GZIPInputStream(in);   
          byte[] buffer = new byte[256];   
          int n;   
          while ((n = gunzip.read(buffer)) >= 0) {   
            out.write(buffer, 0, n);   
          }
          String result = out.toString(); 
          result = URLDecoder.decode(result,"UTF-8");
          return result;  
      }
      catch(Exception e){
    	  e.printStackTrace();
    	  return null;
      }
      finally{
    	  if(in!=null)try{in.close();}catch(Exception e){}
    	  if(out!=null)try{out.close();}catch(Exception e){}
      }
    }   

	
    public static void main(String[] args) throws IOException{
    	System.out.println(uncompress("H4sIAAAAAAAAAyXKuw2AMAwFwF1ejSXyAZNswA5pYmJLlPwqxO4gcfXdaPWsyLgL1laQC1z/Czw4n1wseNBh1+3S45zbdzlIisyOLI1CMUwLiZqngc2k9pOMuuB5AX9NJaxbAAAA"));
    	System.out.println(compress("{\"data\":\"{\\\"id\\\":\\\"1000000037512914\\\"}\",\"requestId\":\"73b94771-f96b-438c-bef2-57ffba08b6ec\"}"));
    }
    
	
	
}
