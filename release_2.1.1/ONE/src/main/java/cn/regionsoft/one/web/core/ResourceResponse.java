package cn.regionsoft.one.web.core;

import java.io.Serializable;


public interface ResourceResponse<T> extends Serializable {
	boolean isSuccess();
	void setSuccess(boolean s);
	String getMsg();
	T getData();
	Integer getRespCode();
	void setMsg(String msg);
	void setData(T data);
}
