package cn.regionsoft.one.core.controller;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import cn.regionsoft.one.enums.RequestMethod;

public class RequestNode {
	private RequestNode parent;
	private NodeType nodeType;
	private String relativePath;
	private List<RequestNode> childs;
	
	/**
	 * Mapping to controller and method
	 */
	private Class<?> controllerClass;
	private Method method;
	private RequestMethod[] requestMethods; 
	private String[] produces;
	private Class<?> returnType;

	public Class<?> getReturnType() {
		return returnType;
	}
	public void setReturnType(Class<?> returnType) {
		this.returnType = returnType;
	}
	public RequestMethod[] getRequestMethods() {
		return requestMethods;
	}
	public void setRequestMethods(RequestMethod[] requestMethods) {
		this.requestMethods = requestMethods;
	}
	public String[] getProduces() {
		return produces;
	}
	public void setProduces(String[] produces) {
		this.produces = produces;
	}
	public RequestNode(NodeType nodeType){
		this.nodeType = nodeType;
	}
	public RequestNode getParent() {
		return parent;
	}
	public void setParent(RequestNode parent) {
		this.parent = parent;
	}
	public List<RequestNode> getChilds() {
		return childs;
	}
	public void setChilds(List<RequestNode> childs) {
		this.childs = childs;
	}
	public NodeType getNodeType() {
		return nodeType;
	}
	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}
	public Class<?> getControllerClass() {
		return controllerClass;
	}
	public void setControllerClass(Class<?> controllerClass) {
		this.controllerClass = controllerClass;
	}
	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
}
