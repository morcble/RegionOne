package cn.regionsoft.one.core.dispatcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.regionsoft.one.annotation.tag.Cookies;
import cn.regionsoft.one.annotation.tag.HeaderInfo;
import cn.regionsoft.one.annotation.tag.PathVariable;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.core.SystemContext;
import cn.regionsoft.one.core.controller.ControllerManager;
import cn.regionsoft.one.core.controller.RequestNode;
import cn.regionsoft.one.core.controller.RequestNodeWrapper;
import cn.regionsoft.one.core.exception.ControllerException;
import cn.regionsoft.one.core.exception.MultipleControllerMappingException;
import cn.regionsoft.one.core.exception.NoControllerMappingException;
import cn.regionsoft.one.reflect.MethodMeta;
import cn.regionsoft.one.utils.PakoUtils;

public class RequestProcessor {
	private static final Logger logger = Logger.getLogger(RequestProcessor.class);
	public static ProcessResponseWrapper process(String requestURI, String method, Map<String, String[]> requestMap,Map<String,String> headerInfo,Map<String,String> cookies) throws Exception {
		try{
			SystemContext systemContext = SystemContext.getInstance();
			Map<String,H2OContext> contextsMap = systemContext.getContextsMap();
			
			List<RequestNodeWrapper> matchedNodes = new ArrayList<RequestNodeWrapper>();
			for(H2OContext context:contextsMap.values()){
				try{
					matchedNodes.add(context.getMatchedRequestNode(requestURI));
				}
				catch(NoControllerMappingException e){}
			}
			
			ProcessResponseWrapper processResponseWrapper = new ProcessResponseWrapper();
			
			RequestNodeWrapper requestNodeWrapper = null; 
			try{
				requestNodeWrapper  = ControllerManager.filterReuqestNodeWrapper(requestURI,matchedNodes);
				processResponseWrapper.setResponse(invoke(requestNodeWrapper,requestMap,headerInfo,cookies));
				processResponseWrapper.setResponseHeader(requestNodeWrapper.getRequestNode().getProduces());
				processResponseWrapper.setReturnType(requestNodeWrapper.getRequestNode().getReturnType());
			}
			catch(NoControllerMappingException e1){
				//logger.error(e1);
				processResponseWrapper.setResponse("Exception: No Controller Mapping Matched");
				processResponseWrapper.setReturnType(null);
			}
			catch(MultipleControllerMappingException e2){
				logger.error(e2);
				processResponseWrapper.setResponse("Exception: Multiple Controller Mapping Found");
				processResponseWrapper.setReturnType(null);
			}
			return processResponseWrapper;
		}
		catch(Exception e){
			logger.error(e);
			throw e;
		}
	}
	
	private static Object invoke(RequestNodeWrapper requestNodeWrapper,Map<String, String[]> requestMap,Map<String,String> headerInfo,Map<String,String> cookies) throws Exception {
		RequestNode requestNode = requestNodeWrapper.getRequestNode();
		LinkedHashMap<String,Object> pathVaribleMap  = requestNodeWrapper.getParaMap();
		
		Method method = requestNode.getMethod();
		
		MethodMeta methodMeta = CommonUtil.getMethodMeta(requestNode.getControllerClass(), method);		
		
		//---------------
		Class<?>[] paraTypes = methodMeta.getParameterTypes();
		int paraLength = paraTypes.length;
		Annotation[][] annotations = methodMeta.getParameterAnnotations();
		
		Annotation[] tmp = null;
		Map tmpMap = null;
		Object[] parasVals = new Object[paraLength];
		for(int i = 0 ; i<paraLength ; i++){
			tmp = annotations[i];
			tmpMap = new HashMap();
			for(Annotation an:tmp){
				tmpMap.put(an.annotationType(),an);
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
			
			/*boolean useCompress = false;
			if("true".equals(headerInfo.get("compress"))){
				useCompress = true;
			}*/
			
			cn.regionsoft.one.annotation.tag.Parameter parameterAno =  (cn.regionsoft.one.annotation.tag.Parameter) tmpMap.get(cn.regionsoft.one.annotation.tag.Parameter.class);
			if(parameterAno!=null){
				String parameterKey = parameterAno.value();
				String[] tmpArray = requestMap.get(parameterKey);
				if(tmpArray!=null){
					if(tmpArray.length==1){
						/*if(useCompress){
							parasVals[i] = PakoUtils.uncompress(tmpArray[0]);
						}
						else{
							parasVals[i] = tmpArray[0];
						}*/
						parasVals[i] = tmpArray[0];
					}
					else{
						parasVals[i] = tmpArray;
					}
				}
				
				continue;
			}
		}
		
		
		
		Object controllerInstance = SystemContext.getInstance().getManagedBean(requestNode.getControllerClass());
		try {
			return method.invoke(controllerInstance, parasVals);
		} catch (Exception e) {
			throw new ControllerException(e);
		}
	}
}
