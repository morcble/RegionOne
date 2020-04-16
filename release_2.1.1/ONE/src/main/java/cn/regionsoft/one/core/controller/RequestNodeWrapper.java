package cn.regionsoft.one.core.controller;

import java.util.LinkedHashMap;

public class RequestNodeWrapper {
	private RequestNode requestNode;
	private LinkedHashMap<String,Object> paraMap;
	public RequestNode getRequestNode() {
		return requestNode;
	}
	public void setRequestNode(RequestNode requestNode) {
		this.requestNode = requestNode;
	}
	public LinkedHashMap<String, Object> getParaMap() {
		return paraMap;
	}
	public void setParaMap(LinkedHashMap<String, Object> paraMap) {
		this.paraMap = paraMap;
	}
}
