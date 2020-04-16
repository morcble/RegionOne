package cn.regionsoft.one.core.combinedreq;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.common.JsonUtil;
import cn.regionsoft.one.core.SystemContext;
import cn.regionsoft.one.core.auth.AdvancedSecurityProvider;
import cn.regionsoft.one.core.dispatcher.ProcessResponseWrapper;
import cn.regionsoft.one.core.dispatcher.WebRequestProcessor;
import cn.regionsoft.one.enums.UserToDoAction;
import cn.regionsoft.one.rpc.common.ServerConstant;
import cn.regionsoft.one.web.core.RespCode;
import cn.regionsoft.one.web.wrapper.ResourceResWrapper;
import cn.regionsoft.one.web.wrapper.WebReqWrapper;

public class SingleRequestThread implements Callable<SingleResponse>{
	private SingleRequest singleRequest;
	private Map<String, String[]> requestMap = new HashMap<String, String[]>();
	private Map<String, String> headerInfo;
	private HttpServletRequest request;
	private HttpServletResponse response;
	
	private Map<String, String> cookies;
	


	public SingleRequestThread(SingleRequest singleRequest, Map<String, String> headerInfo,Map<String, String> cookies, HttpServletRequest request,HttpServletResponse response) {
		this.singleRequest = singleRequest;
		if(request.getParameterMap()!=null)
			this.requestMap.putAll(request.getParameterMap());
		this.headerInfo = headerInfo;
		this.request = request;
		this.response = response;
		this.cookies = cookies;
	}

	@Override
	public SingleResponse call() throws Exception {
		SingleResponse singleResponse = new SingleResponse();
		singleResponse.setRequestId(singleRequest.getRequestId());
		try{
			WebReqWrapper webReqWrapper = new WebReqWrapper();
			webReqWrapper.setData(singleRequest.getReqData());
			webReqWrapper.setRequestId(singleRequest.getRequestId());
			requestMap.put("requestStr", new String[]{JsonUtil.objectToJson(webReqWrapper)});
			
			String pageUrl = singleRequest.getPageUrl();
			int paraIndex = pageUrl.indexOf("?");
			String reqUrl = pageUrl;
			if(paraIndex!=-1){
				reqUrl = pageUrl.substring(0, paraIndex);
				String paras = pageUrl.substring(paraIndex+1);
				String[] paraArray = paras.split("&");
				for(String tmp:paraArray){
					String[] tmpPara = tmp.split("=");
					if(tmpPara.length==2){
						requestMap.put(tmpPara[0], new String[]{tmpPara[1]});
					}
				}
			} 
			//headerInfo.put("compress", "false");
			ProcessResponseWrapper prw = WebRequestProcessor.process(reqUrl,singleRequest.getMethod(),requestMap,headerInfo,cookies,request,response);
			String responseMsg = (String) prw.getResponse();
			
			singleResponse.setResponse(responseMsg);
		}
		catch(Exception e){
			String responseMsg = JsonUtil.objectToJson(ResourceResWrapper.failResult("Error:", singleRequest, RespCode._508));
			singleResponse.setResponse(responseMsg);
		}
		/*finally{
			SystemContext.getInstance().releaseResources();
		}*/
		
		return singleResponse;
	}

}
