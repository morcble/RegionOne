package cn.regionsoft.one.web.core;

import java.io.Serializable;

import cn.regionsoft.one.web.wrapper.PaginationTO;


public interface ResourceRequest<T> extends Serializable {
	public PaginationTO getPaginationTO();
	public void setPaginationTO(PaginationTO paginationTO) ;
	public T getData() ;
	public void setData(T data);
}