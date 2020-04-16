package cn.regionsoft.one.web.wrapper;

import java.io.Serializable;

import cn.regionsoft.one.web.core.ResourceRequest;


public class ResourceReqWrapper<T> implements ResourceRequest<T>, Serializable {
	private static final long serialVersionUID = 8495299161890084543L;
	private PaginationTO paginationTO;
	private T data;
	public ResourceReqWrapper(PaginationTO paginationTO, T data) {
		super();
		this.paginationTO = paginationTO;
		this.data = data;
	}
	public PaginationTO getPaginationTO() {
		return paginationTO;
	}
	public void setPaginationTO(PaginationTO paginationTO) {
		this.paginationTO = paginationTO;
	}
	public T getData() {
		return data;
	}
	public void setData(T data) {
		this.data = data;
	}
	
	public static <T> ResourceRequest<T> listRequest(PaginationTO paginationTO, T data) {
		return new ResourceReqWrapper<T>(paginationTO,data);
	}
	
}
