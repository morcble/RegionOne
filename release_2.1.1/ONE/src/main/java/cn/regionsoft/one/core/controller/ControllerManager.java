package cn.regionsoft.one.core.controller;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import cn.regionsoft.one.annotation.InstanceAnoType;
import cn.regionsoft.one.annotation.tag.RequestMapping;

import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.exception.MultipleControllerMappingException;
import cn.regionsoft.one.core.exception.NoControllerMappingException;

public class ControllerManager {
	private RequestNode rootNode = null;
	
	public ControllerManager(H2OContext context){
		rootNode = new RequestNode(NodeType.PATH);
		rootNode.setRelativePath("");
		
		HashSet<Class<?>> controllers = context.getAnnotatedClassHub().getClassSetByAnnotation(InstanceAnoType.Controller);
		
		RequestMapping requestMapping = null;
		if(controllers!=null){
			for(Class<?> controller:controllers){
				requestMapping = controller.getAnnotation(RequestMapping.class);
				registerController(controller,requestMapping);
			}
		}
	}
	
	/**
	 * get match request node by requestURI
	 * @param requestURI
	 * @return
	 * @throws Exception
	 */
	public RequestNodeWrapper getMatchedRequestNode(String requestURI) throws Exception{
		List<String> pathLs = new ArrayList<String>();
		String[] array = requestURI.split("/");
		for(String tmp:array){
			if(!CommonUtil.isEmpty(tmp))pathLs.add(tmp);
		}
		
		List<RequestNodeWrapper> matchedNodes = new ArrayList<RequestNodeWrapper>();
		checkMatchedRequestNode(rootNode,pathLs,0,null,matchedNodes);
		
		return filterReuqestNodeWrapper(requestURI,matchedNodes);
	}
	
	public static RequestNodeWrapper filterReuqestNodeWrapper(String requestURI,List<RequestNodeWrapper> matchedNodes) throws Exception{
		if(matchedNodes.size()==0){
			throw new NoControllerMappingException(requestURI);
		}
		else if(matchedNodes.size()>1){
			StringBuilder sb = new StringBuilder();
			sb.append("Multiple controller mapping are found \r\n");
			
			for(RequestNodeWrapper tmp:matchedNodes){
				sb.append(tmp.getRequestNode().getControllerClass().getName());
				sb.append(".");
				sb.append(tmp.getRequestNode().getMethod().getName());
				sb.append("\r\n");
			}
			
			throw new MultipleControllerMappingException(sb.toString());
		}
		else{
			return matchedNodes.get(0);
		}
	}
	
	private void checkMatchedRequestNode(RequestNode targetNode ,List<String> pathLs ,int depth ,LinkedHashMap<String,Object> paraMap ,List<RequestNodeWrapper> matchedNodes){
		if(depth == pathLs.size()){
			if(targetNode.getControllerClass()!=null){
				RequestNodeWrapper requestNodeWrapper = new RequestNodeWrapper();
				requestNodeWrapper.setRequestNode(targetNode);
				requestNodeWrapper.setParaMap(paraMap);
				matchedNodes.add(requestNodeWrapper);
			}
			return;
		}
		
		if(targetNode.getChilds()==null||targetNode.getChilds().size()==0){
			return;
		}
		String relativePath = pathLs.get(depth);
		
		int nextDepth = depth +1;
		for(RequestNode requestNode:targetNode.getChilds()){
			if(requestNode.getNodeType() == NodeType.PATH){
				if(relativePath.equals(requestNode.getRelativePath())){
					checkMatchedRequestNode(requestNode,pathLs,nextDepth,paraMap,matchedNodes);
				}
				else{
					continue;
				}
			}
			else if(requestNode.getNodeType() == NodeType.VARIABLE){
				LinkedHashMap<String,Object> tmpMap = new LinkedHashMap<String,Object>();
				if(paraMap!=null){
					tmpMap.putAll(paraMap);
				}
				tmpMap.put(requestNode.getRelativePath(), relativePath);
				checkMatchedRequestNode(requestNode,pathLs,nextDepth,tmpMap,matchedNodes);
			}
		}
		
		++depth;
	}
	
	private void registerController(Class<?> controllerClass ,RequestMapping requestMapping){
		String contextPath = null;
		if(requestMapping==null) {
			contextPath = "";
		}
		else{
			contextPath = requestMapping.value();
		}
		
		List<String> pathLs = new ArrayList<String>();
		if(contextPath!=null){
			String[] array = contextPath.split("/");
			for(String tmp:array){
				if(!CommonUtil.isEmpty(tmp))pathLs.add(tmp);
			}
			
			
			RequestNode contextNode = refreshNode(rootNode,pathLs,0);
			
			/**
			 * resolve method context
			 */
			Method[] methods = controllerClass.getDeclaredMethods();
			RequestMapping methodReqMapping = null;
			for(Method method:methods){
				methodReqMapping = method.getAnnotation(RequestMapping.class);
				if(methodReqMapping==null) continue;
				
				contextPath = methodReqMapping.value();
				pathLs.clear();
				array = contextPath.split("/");
				for(String tmp:array){
					if(!CommonUtil.isEmpty(tmp))pathLs.add(tmp);
				}
				RequestNode methodNode = refreshNode(contextNode,pathLs,0);
				methodNode.setControllerClass(controllerClass);
				methodNode.setMethod(method);
				methodNode.setRequestMethods(methodReqMapping.method());
				methodNode.setProduces(methodReqMapping.responseHeader());
				methodNode.setReturnType(method.getReturnType());
				/*ResponseBody responseBody = method.getAnnotation(ResponseBody.class);
				if(responseBody!=null){
					methodNode.setReturnType(responseBody);
				}*/
			}
		}
		
		
	}
	
	
	private RequestNode refreshNode(RequestNode targetNode, List<String> pathLs , int depth){
		if(pathLs.size() == depth) {
			return targetNode;
		}
		NodeType noteType = NodeType.PATH;
		String relativePath = pathLs.get(depth);
		if(relativePath.startsWith("${") && relativePath.endsWith("}")){//path 参数
			relativePath = relativePath.substring(2, relativePath.length()-1);
			noteType = NodeType.VARIABLE;
		}
		RequestNode newNode = new RequestNode(noteType);
		newNode.setParent(targetNode);
		newNode.setRelativePath(relativePath);
		
		List<RequestNode> childs = targetNode.getChilds();
		if(childs==null){
			childs = new ArrayList<RequestNode>();
			targetNode.setChilds(childs);
		}
		childs.add(newNode);
		
		return refreshNode(newNode,pathLs,++depth);
	}

}
