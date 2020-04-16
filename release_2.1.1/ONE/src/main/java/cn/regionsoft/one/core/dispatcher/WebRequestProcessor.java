package cn.regionsoft.one.core.dispatcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import cn.regionsoft.one.annotation.tag.Cookies;
import cn.regionsoft.one.annotation.tag.HeaderInfo;
import cn.regionsoft.one.annotation.tag.Parameter;
import cn.regionsoft.one.annotation.tag.PathVariable;
import cn.regionsoft.one.annotation.tag.RequestMapping;
import cn.regionsoft.one.assist.method.MethodProxyAssist;
import cn.regionsoft.one.assist.method.MethodProxyWithValueReturn;
import cn.regionsoft.one.caches.LocalCacheUtil;
import cn.regionsoft.one.common.AESUtil;
import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.common.JsonUtil;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.core.I18nMessageManager;
import cn.regionsoft.one.core.RequestInfoHolder;
import cn.regionsoft.one.core.SystemContext;
import cn.regionsoft.one.core.auth.AdvancedSecurityProvider;
import cn.regionsoft.one.core.auth.BasicSecurityProvider;
import cn.regionsoft.one.core.auth.dto.BasicSecurityResultDto;
import cn.regionsoft.one.core.auth.dto.RequestInfoDto;
import cn.regionsoft.one.core.controller.ControllerManager;
import cn.regionsoft.one.core.controller.RequestNode;
import cn.regionsoft.one.core.controller.RequestNodeWrapper;
import cn.regionsoft.one.core.exception.ControllerException;
import cn.regionsoft.one.core.exception.MultipleControllerMappingException;
import cn.regionsoft.one.core.exception.NoControllerMappingException;
import cn.regionsoft.one.enums.RequestMethod;
import cn.regionsoft.one.enums.UserToDoAction;
import cn.regionsoft.one.properties.ConfigUtil;
import cn.regionsoft.one.reflect.MethodMeta;
import cn.regionsoft.one.rpc.common.ServerConstant;
import cn.regionsoft.one.web.core.RespCode;
import cn.regionsoft.one.web.wrapper.ResourceResWrapper;


public class WebRequestProcessor {
	private static final Logger logger = Logger.getLogger(WebRequestProcessor.class);
	private static String secfilterConfig = ConfigUtil.getProperty(ServerConstant.SECURITY_FILTER);
	private static final String JSON_HEADER = "Content-Type == application/json;charset=UTF-8";
	
	private static final String PROMPT_LOGIN = "login.required";
	private static final String PROMPT_403 = "access.forbidden";
	private static final String END_SUFFIX = "/";
	
	private static final String LOCALE = "locale";
	
	private static SystemContext systemContext = null;
	private static Map<String,H2OContext> contextsMap = null;
	//private static SecurityFilter  securityFilter = null;
	static{
		systemContext = SystemContext.getInstance();
		contextsMap = systemContext.getContextsMap();
		/*if(!CommonUtil.isEmpty(secfilterConfig)){
			try{
				Class tmp = Class.forName(secfilterConfig);
				securityFilter= (SecurityFilter) tmp.newInstance();
			}
			catch(Exception e){
				logger.warn(e);
			}	
		}*/
	}

	public static ProcessResponseWrapper process(String requestURI, String method, Map<String, String[]> requestMap,Map<String,String> headerInfo,Map<String,String> cookies,HttpServletRequest request, HttpServletResponse response) throws Exception {
		try{
			//security filter begin
			if(requestURI.endsWith(END_SUFFIX)){
				requestURI = requestURI.substring(0, requestURI.length()-1);
			}

			String loginToken = null;
			RequestInfoHolder.getInstance().setRequestInfo(null);
			UserToDoAction userToDoAction = UserToDoAction.VALID_ACCESS;
			
			RequestInfoDto requestInfoDto = new RequestInfoDto();
			requestInfoDto.setLocale(cookies.get(LOCALE));

			RequestInfoHolder.getInstance().setRequestInfo(requestInfoDto);
			//String secfilterConfig = PropertiesUtil.getConfigProperty(ServerConstant.SECURITY_FILTER);
			/*if(securityFilter!=null){
				SecurityFilterResult securityFilterResult = securityFilter.checkAccess(requestURI, method, requestMap, headerInfo, request, response);
				userToDoAction = securityFilterResult.getUserToDoAction();
				
				requestInfoDto.setLoginAccount(securityFilterResult.getAccount());
				requestInfoDto.setToken(securityFilterResult.getToken());
				loginToken = securityFilterResult.getToken();
			}*/
			
			if(userToDoAction == UserToDoAction.VALID_ACCESS){
				if(loginToken==null){
					loginToken = cookies.get(ServerConstant.REGION_TOKEN);
				}
				BasicSecurityProvider basicSecurity = systemContext.getBasicSecurityProvider();
				BasicSecurityResultDto basicSecurityResultDto = null;
				if(basicSecurity!=null){
					basicSecurityResultDto = basicSecurity.validateAccess(loginToken, requestURI, request, response,cookies);
					userToDoAction = basicSecurityResultDto.getUserToDoAction();
					
					if(userToDoAction == UserToDoAction.VALID_ACCESS){
						AdvancedSecurityProvider advancedSecurityProvider = systemContext.getAdvancedSecurityProvider();
						if(advancedSecurityProvider!=null){
							userToDoAction = advancedSecurityProvider.checkAccess(basicSecurityResultDto.getAccount(),requestURI, headerInfo,cookies, request,response);
						}
					}
				}
				
				/**
				 * save login info into thread
				 */
				if(basicSecurityResultDto!=null){
					requestInfoDto.setLoginAccount(basicSecurityResultDto.getAccount());
					requestInfoDto.setToken(loginToken);
				}
				
			}
			//security filter end
			if(userToDoAction != UserToDoAction.VALID_ACCESS){
				//donothing;
				ProcessResponseWrapper processResponseWrapper = new ProcessResponseWrapper();
				if(userToDoAction == UserToDoAction.NEED_TO_LOGIN){
					processResponseWrapper.setResponse(JsonUtil.objectToJson(ResourceResWrapper.failResult(I18nMessageManager.getMessageWithDefault(PROMPT_LOGIN, "please login first"), RespCode._401)));
				}
				else if(userToDoAction == UserToDoAction.ACCESS_FORBIDDEN){
					processResponseWrapper.setResponse(JsonUtil.objectToJson(ResourceResWrapper.failResult(I18nMessageManager.getMessageWithDefault(PROMPT_403, "access forbidden"), RespCode._403)));
				}
				else{
					processResponseWrapper.setResponse(JsonUtil.objectToJson(ResourceResWrapper.failResult(I18nMessageManager.getMessageWithDefault(PROMPT_403, "access forbidden"), RespCode._403)));
				}
				processResponseWrapper.setResponseHeader(new String[]{JSON_HEADER});
				processResponseWrapper.setReturnType(null);
				return processResponseWrapper;
			}

			ProcessResponseWrapper processResponseWrapper = new ProcessResponseWrapper();
			try{
				RequestNodeWrapper requestNodeWrapper = (RequestNodeWrapper) LocalCacheUtil.get(LocalCacheUtil.URL_NODEMAP_CACHE+requestURI);//TODO Improve for multiple threads
				if(requestNodeWrapper==null){
					List<RequestNodeWrapper> matchedNodes = new ArrayList<RequestNodeWrapper>();
					for(H2OContext context:contextsMap.values()){
						try{
							matchedNodes.add(context.getMatchedRequestNode(requestURI));
						}
						catch(NoControllerMappingException e){}
					}
					requestNodeWrapper  = ControllerManager.filterReuqestNodeWrapper(requestURI,matchedNodes);
					
					LinkedHashMap<String,Object> pathVaribleMap  = requestNodeWrapper.getParaMap();
					if(pathVaribleMap==null||pathVaribleMap.size()==0){
						LocalCacheUtil.put(LocalCacheUtil.URL_NODEMAP_CACHE+requestURI, requestNodeWrapper);
					}
				}
				RequestMapping requestMapping = requestNodeWrapper.getRequestNode().getMethod().getAnnotation(RequestMapping.class);
				
				if(Boolean.valueOf((ConfigUtil.getProperty(Constants.AES_ENABLED))))//加密总开关
					processResponseWrapper.setEncript(true);
				
				if(requestMapping.ignoreEncryption()) {
					processResponseWrapper.setEncript(false);
				}
				
				RequestMethod[] methods = requestMapping.method();
				boolean methodMatched = false;
				method = method.toUpperCase();
				for(RequestMethod requestMethod:methods){
					if(requestMethod.getVal().equals(method)){
						methodMatched = true;
						break;
					}
				}
				if(!methodMatched){
					processResponseWrapper.setResponse(JsonUtil.objectToJson(ResourceResWrapper.failResult(method +" is not supported by " +requestNodeWrapper.getRequestNode().getControllerClass().getSimpleName()+"."+requestNodeWrapper.getRequestNode().getMethod().getName(), RespCode._404)));
					processResponseWrapper.setResponseHeader(new String[]{JSON_HEADER});
					processResponseWrapper.setReturnType(null);
				}
				else{
					String responseStr = (String) invoke(requestNodeWrapper,requestMap,headerInfo,cookies,request,response,requestURI,method,processResponseWrapper.isEncript());
					processResponseWrapper.setResponse(responseStr);
					processResponseWrapper.setResponseHeader(requestNodeWrapper.getRequestNode().getProduces());
					processResponseWrapper.setReturnType(requestNodeWrapper.getRequestNode().getReturnType());
				}
			}
			catch(NoControllerMappingException e1){
				logger.warn(e1);
				processResponseWrapper.setResponse(JsonUtil.objectToJson(ResourceResWrapper.failResult(requestURI+" 服务不存在", RespCode._404)));
				processResponseWrapper.setResponseHeader(new String[]{JSON_HEADER});
				processResponseWrapper.setReturnType(null);
			}
			catch(MultipleControllerMappingException e2){
				logger.error(e2);
				processResponseWrapper.setResponse(JsonUtil.objectToJson(ResourceResWrapper.failResult("多个Controller对应一个URL"+requestURI, RespCode._409)));
				processResponseWrapper.setResponseHeader(new String[]{JSON_HEADER});
				processResponseWrapper.setReturnType(null);
			}
			catch(ControllerException e3){
				logger.error(e3);
				processResponseWrapper.setResponse(JsonUtil.objectToJson(ResourceResWrapper.failResult(e3.getCause().getMessage(), RespCode._508)));
				processResponseWrapper.setResponseHeader(new String[]{JSON_HEADER});
				processResponseWrapper.setReturnType(null);
			}
			finally{
				/**
				 * clean login info from thread
				 */
				RequestInfoHolder.getInstance().setRequestInfo(null);
			}
			return processResponseWrapper;
		}
		catch(Exception e){
			logger.error(e);
			throw e;
		}
	}
	
	private static Object invoke(RequestNodeWrapper requestNodeWrapper,Map<String, String[]> requestMap,Map<String,String> headerInfo,Map<String,String> cookies,HttpServletRequest request, HttpServletResponse response,String requestURI,String httpMethod, boolean encrypt) throws ControllerException {
		RequestNode requestNode = requestNodeWrapper.getRequestNode();
		
	
//		Object[] parameters = resolveParas(requestNodeWrapper,requestMap,headerInfo,cookies,request, response,requestURI);
//		Object srcObj = systemContext.getManagedBean(requestNode.getControllerClass());
//		Class[] parameterTypes = requestNode.getMethod().getParameterTypes();
//
//		try {
//			MethodProxyWithValueReturn proxy = (MethodProxyWithValueReturn) MethodProxyAssist.genMethodProxy(srcObj,requestNode.getMethod().getName(),parameterTypes,true);
//			return proxy.excute(srcObj, parameters);
//		} catch (Exception e) {
//			throw new ControllerException(e);
//		}
		
		try {
			return requestNode.getMethod().invoke(systemContext.getManagedBean(requestNode.getControllerClass()), resolveParas(requestNodeWrapper,requestMap,headerInfo,cookies,request, response,requestURI,httpMethod,encrypt));
		} catch (Exception e) {
			throw new ControllerException(e); 
		}
	}


	private static Object[] resolveParas(RequestNodeWrapper requestNodeWrapper,Map<String, String[]> requestMap,Map<String,String> headerInfo
			,Map<String,String> cookies,HttpServletRequest request, HttpServletResponse response,String requestURI,String httpMethod, boolean encrypt) throws Exception{
		RequestNode requestNode = requestNodeWrapper.getRequestNode();
		LinkedHashMap<String,Object> pathVaribleMap  = requestNodeWrapper.getParaMap();
		Method method = requestNode.getMethod();
		
		MethodMeta methodMeta = CommonUtil.getMethodMeta(requestNode.getControllerClass(), method);
		Map<Integer,String> paraNamesMirror = methodMeta.getParaNamesMirror();
		//---------------
		Class<?>[] paraTypes = methodMeta.getParameterTypes();
		int paraLength = paraTypes.length;
		Annotation[][] annotations = methodMeta.getParameterAnnotations();
		
		Annotation[] tmp = null;
		Object[] parasVals = new Object[paraLength];
		boolean aesEnabled = Boolean.valueOf(ConfigUtil.getProperty(Constants.AES_ENABLED));
		
		if(!encrypt)aesEnabled=false;//单个controller可以申明忽略加密,覆盖全局设置
		
		String aesKey = null;
		String aesIv = null;
		if(aesEnabled) {
			aesKey = ConfigUtil.getProperty(Constants.AES_KEY);
			aesIv = ConfigUtil.getProperty(Constants.AES_IV);
		}
		
		for(int i = 0 ; i<paraLength ; i++){
			if(paraTypes[i]==HttpServletRequest.class){
				parasVals[i] = request;
				continue;
			}
			if(paraTypes[i]==HttpServletResponse.class){
				parasVals[i] = response;
				continue;
			}
						
			tmp = annotations[i];
			//没有注解的情况,默认为parameter
			if(tmp.length==0) {
				String parameterKey = paraNamesMirror.get(i);
				String[] tmpArray = requestMap.get(parameterKey);
				if(tmpArray!=null){
					if(httpMethod.equals(Constants.POST)) {
						if(aesEnabled) {//解密AES
							for(int k = 0 ; k<tmpArray.length;k++) {
								tmpArray[k] = new String(AESUtil.decriptCBC(tmpArray[k],aesKey, aesIv),Constants.UTF8);
							}
						}
					}

					if(tmpArray.length==1){
						parasVals[i] = tmpArray[0];
					}
					else{
						parasVals[i] = tmpArray;
					}
				}
				continue;
			}
			
			for(Annotation an:tmp){
				if(an.annotationType()==Parameter.class) {
					String parameterKey = ((Parameter)an).value();
					if(CommonUtil.isEmpty(parameterKey)) {//没有value则从方法里的属性名里取
						parameterKey = paraNamesMirror.get(i);
					}
					String[] tmpArray = requestMap.get(parameterKey);
					if(tmpArray!=null){
						if(httpMethod.equals(Constants.POST)) {
							if(aesEnabled) {//解密AES
								for(int k = 0 ; k<tmpArray.length;k++) {
									tmpArray[k] = new String(AESUtil.decriptCBC(tmpArray[k],aesKey, aesIv),Constants.UTF8);
								}
							}
						}

						if(tmpArray.length==1){
							parasVals[i] = tmpArray[0];
						}
						else{
							parasVals[i] = tmpArray;
						}
					}
					break;
				}
				else if(an.annotationType()==HeaderInfo.class) {
					parasVals[i] = headerInfo;
					break;
				}
				else if(an.annotationType()==Cookies.class) {
					parasVals[i] = cookies;
					break;
				}
				else if(an.annotationType()==PathVariable.class) {
					String pathVaribleKey = ((PathVariable)an).value();
					if(paraTypes[i] == String.class){
						parasVals[i] = pathVaribleMap.get(pathVaribleKey);
					}
					else if(paraTypes[i] == Long.class){
						parasVals[i] = Long.valueOf((String) pathVaribleMap.get(pathVaribleKey));
					}
					else if(paraTypes[i] == Integer.class){
						parasVals[i] = Integer.valueOf((String) pathVaribleMap.get(pathVaribleKey));
					}
					else if(paraTypes[i] == Double.class){
						parasVals[i] = Double.valueOf((String) pathVaribleMap.get(pathVaribleKey));
					}
					else if(paraTypes[i] == Float.class){
						parasVals[i] = Float.valueOf((String) pathVaribleMap.get(pathVaribleKey));
					}
					break;
				}
			}

		}
		
		return parasVals;
	}
	
	
	private static Object[] resolveParasOld(RequestNodeWrapper requestNodeWrapper,Map<String, String[]> requestMap,Map<String,String> headerInfo,Map<String,String> cookies,HttpServletRequest request, HttpServletResponse response,String requestURI){
		RequestNode requestNode = requestNodeWrapper.getRequestNode();
		LinkedHashMap<String,Object> pathVaribleMap  = requestNodeWrapper.getParaMap();
		Method method = requestNode.getMethod();
		
		//---------------
		Class<?>[] paraTypes = method.getParameterTypes();
		int paraLength = paraTypes.length;
		Annotation[][] annotations = method.getParameterAnnotations();
		
		Annotation[] tmp = null;
		Map tmpMap = null;
		Object[] parasVals = new Object[paraLength];
		for(int i = 0 ; i<paraLength ; i++){
			if(paraTypes[i]==HttpServletRequest.class){
				parasVals[i] = request;
				continue;
			}
			if(paraTypes[i]==HttpServletResponse.class){
				parasVals[i] = response;
				continue;
			}
						
			tmp = annotations[i];
			tmpMap = new HashMap();
			for(Annotation an:tmp){
				tmpMap.put(an.annotationType(),an);
			}
			
			Parameter parameterAno =  (Parameter) tmpMap.get(Parameter.class);
			if(parameterAno!=null){
				String parameterKey = parameterAno.value();
				String[] tmpArray = requestMap.get(parameterKey);
				if(tmpArray!=null){
					if(tmpArray.length==1){
						parasVals[i] = tmpArray[0];
					}
					else{
						parasVals[i] = tmpArray;
					}
				}
				
				continue;
			}
			
			if(tmpMap.containsKey(HeaderInfo.class)){
				parasVals[i] = headerInfo;
				continue;
			}
			
			if(tmpMap.containsKey(Cookies.class)){
				parasVals[i] = cookies;
				continue;
			}

			PathVariable pathVariableAnno = (PathVariable) tmpMap.get(PathVariable.class);
			if(pathVariableAnno!=null){
				String pathVaribleKey = pathVariableAnno.value();
				if(paraTypes[i] == String.class){
					parasVals[i] = pathVaribleMap.get(pathVaribleKey);
				}
				else if(paraTypes[i] == Long.class){
					parasVals[i] = Long.valueOf((String) pathVaribleMap.get(pathVaribleKey));
				}
				else if(paraTypes[i] == Integer.class){
					parasVals[i] = Integer.valueOf((String) pathVaribleMap.get(pathVaribleKey));
				}
				else if(paraTypes[i] == Double.class){
					parasVals[i] = Double.valueOf((String) pathVaribleMap.get(pathVaribleKey));
				}
				else if(paraTypes[i] == Float.class){
					parasVals[i] = Float.valueOf((String) pathVaribleMap.get(pathVaribleKey));
				}
				continue;
			}

		}
		
		return parasVals;
	}
}
