package cn.regionsoft.one.core.combinedreq;

import cn.regionsoft.one.core.CommonUtil;

public class SingleRequest {
	private String requestId;
	private String pageUrl;
	private String reqData;
	private String method;
	public String getPageUrl() {
		return pageUrl;
	}
	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}
	public String getReqData() {
		return reqData;
	}
	public void setReqData(String reqData) {
		this.reqData = reqData;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	public String toString(){
		return CommonUtil.instanceToString(this,false);
	}
}
