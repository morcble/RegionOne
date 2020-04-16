package cn.regionsoft.one.core;

import cn.regionsoft.one.core.auth.dto.RequestInfoDto;

public class RequestInfoHolder {
	private static RequestInfoHolder instance = new RequestInfoHolder();
	private ThreadLocal<RequestInfoDto> cache = new ThreadLocal<RequestInfoDto>();
	private RequestInfoHolder(){}
	
	public static RequestInfoHolder getInstance(){
		return instance;
	}
	
	static{
		if(instance==null){
			instance = new RequestInfoHolder();
		}
	}
	
	public RequestInfoDto getRequestInfo(){
		return cache.get();
	}
	
	public void setRequestInfo(RequestInfoDto requestInfo){
		cache.set(requestInfo);
	}
}
