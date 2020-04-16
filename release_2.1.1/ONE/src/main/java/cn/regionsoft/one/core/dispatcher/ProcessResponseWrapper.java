package cn.regionsoft.one.core.dispatcher;

import java.lang.annotation.Annotation;

import cn.regionsoft.one.standalone.RegionHttpResponse;

public class ProcessResponseWrapper {
	private Object response;
	private String[] responseHeader;
	private Class<?> returnType;
	private RegionHttpResponse httprequestResponse;
	private boolean encript;
	public RegionHttpResponse getHttprequestResponse() {
		return httprequestResponse;
	}
	public void setHttprequestResponse(RegionHttpResponse httprequestResponse) {
		this.httprequestResponse = httprequestResponse;
	}
	public Object getResponse() {
		return response;
	}
	public void setResponse(Object response) {
		this.response = response;
	}
	public String[] getResponseHeader() {
		return responseHeader;
	}
	public void setResponseHeader(String[] responseHeader) {
		this.responseHeader = responseHeader;
	}
	
	public Class<?> getReturnType() {
		return returnType;
	}
	public void setReturnType(Class<?> returnType) {
		this.returnType = returnType;
	}


	private boolean completed = false;
	public boolean isCompleted() {
		return completed;
	}
	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
	public boolean isEncript() {
		return encript;
	}
	public void setEncript(boolean encript) {
		this.encript = encript;
	}
	
}
