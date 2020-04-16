package cn.regionsoft.one.web.wrapper;

import java.io.Serializable;
import cn.regionsoft.one.web.core.WebRequest;


public class WebReqWrapper implements WebRequest, Serializable{
	private static final long serialVersionUID = 6414961875359963122L;
	private String requestId;
	private String rsAppId;//for cloud app only
	private String data;
	
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String getRsAppId() {
		return rsAppId;
	}
	public void setRsAppId(String rsAppId) {
		this.rsAppId = rsAppId;
	}
	
}
