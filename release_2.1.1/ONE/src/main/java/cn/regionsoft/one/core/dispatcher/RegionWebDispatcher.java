package cn.regionsoft.one.core.dispatcher;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.regionsoft.one.core.SYSEnvSetup;
import cn.regionsoft.one.core.SystemContext;
import cn.regionsoft.one.utils.PakoUtils;
import cn.regionsoft.one.core.CommonUtil;

public class RegionWebDispatcher extends HttpServlet {
	private static final long serialVersionUID = 8919257255115367298L;
	
	public RegionWebDispatcher(){
		
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);  
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		
		String reqURI = request.getRequestURI();
		reqURI = reqURI.replace(this.getServletContext().getContextPath(), "");
		reqURI = reqURI.replaceFirst("/region", "");

		String method = request.getMethod().toUpperCase();
		Map<String, String[]> requestMap = request.getParameterMap();
		
		PrintWriter pw =null;
		try {
			Map<String,String> headerInfo = new HashMap<String,String>();
			Map<String,String> cookiesMap = new HashMap<String,String>();
			
			Enumeration<String> headers = request.getHeaderNames();
			while(headers.hasMoreElements()){
				String key = headers.nextElement();
				if(key.equals("cookie")){
					String cookie = request.getHeader(key);
					if(CommonUtil.isEmpty(cookie))continue;
					String[] array = cookie.split(";");
					for(String tmp:array){
						String[] tmpArray = tmp.split("=");
						cookiesMap.put(tmpArray[0].trim(), tmpArray[1].trim());
					}
				}
				else{
					headerInfo.put(key, request.getHeader(key));
				}
			}

			
			pw = response.getWriter();
			ProcessResponseWrapper prw = WebRequestProcessor.process(reqURI,method,requestMap,headerInfo,cookiesMap,request,response);
			String responseMsg = (String) prw.getResponse();

			String[] responseHeader = prw.getResponseHeader();
			if(responseHeader!=null){
				for(String tmpHeader : responseHeader){
					String tmpArray[]  = tmpHeader.split("==");
					if(tmpArray!=null&&tmpArray.length==2){
						response.setHeader(tmpArray[0].trim(), tmpArray[1].trim());
					}
				}
			}
			

			/*if("true".equals(request.getHeader("compress"))){
				responseMsg = PakoUtils.compress(responseMsg);
				response.setHeader("compress", "true");
				response.setHeader("Content-Type", "text/html;charset:utf-8");
			}*/
			if(responseMsg!=null)pw.write(responseMsg);
		} catch (Exception e) {
			throw new ServletException(e);
		}
		finally{
			try{
				if(pw!=null){
					//pw.flush();
					CommonUtil.closeQuietly(pw);
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}

		}
	}
	
	/**
	 * http://localhost:8088/PersistenceWeb/region/xxx/aaversion/aaversionlist/3/fenglj?qs=123%2B55
	 * @param args
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String[] args) throws UnsupportedEncodingException{
		String a = "123+55";
		System.out.println(URLEncoder.encode(a,"UTF-8"));
	}
}


