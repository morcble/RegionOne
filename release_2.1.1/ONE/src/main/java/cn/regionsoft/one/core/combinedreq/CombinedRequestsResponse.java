package cn.regionsoft.one.core.combinedreq;

import java.util.ArrayList;
import java.util.List;

public class CombinedRequestsResponse {
	private String requestId;
	
	private List<SingleResponse> responseList = new ArrayList<SingleResponse>();

	public List<SingleResponse> getResponseList() {
		return responseList;
	}

	public void setResponseList(List<SingleResponse> responseList) {
		this.responseList = responseList;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
}
