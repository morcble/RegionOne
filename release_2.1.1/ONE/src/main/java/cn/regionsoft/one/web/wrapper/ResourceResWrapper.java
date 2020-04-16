package cn.regionsoft.one.web.wrapper;

import java.io.Serializable;

import cn.regionsoft.one.web.core.ResourceResponse;
import cn.regionsoft.one.web.core.RespCode;

/**
 * both for rpc response and http response
 * @author fenglj
 *
 * @param <T>
 */
public class ResourceResWrapper<T> implements ResourceResponse<T>, Serializable {
	private static final long serialVersionUID = -3013264006300138561L;
	
	private boolean success;
	private String msg;
	private T data;
	private Integer respCode;
	
	
	public ResourceResWrapper(boolean success, String message, T data, RespCode respCode) {
		this.success = success;
		this.msg = message;
		this.data = data;
		this.respCode = respCode.getRespCode();
	}

	public boolean isSuccess() {
		return success;
	}

	public T getData() {
		return data;
	}

	public String getMsg() {
		return msg;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public void setData(T data) {
		this.data = data;
	}

	public Integer getRespCode() {
		return respCode;
	}

	public void setRespCode(Integer respCode) {
		this.respCode = respCode;
	}
	
	
	public static <T> ResourceResponse<T> failResult(String message, T data,RespCode respCode) {
		return new ResourceResWrapper<T>(false, message, data,respCode);
	}
	
	public static <T> ResourceResponse<T> failResult(String message, RespCode respCode) {
		return new ResourceResWrapper<T>(false, message, null,respCode);
	}

	public static <T> ResourceResponse<T> successResult(String message, T data) {
		return new ResourceResWrapper<T>(true, message, data,RespCode._200);
	}
	
	public static <T> ResourceResponse<T> successResult(T data) {
		return new ResourceResWrapper<T>(true, null, data,RespCode._200);
	}
	
	public static <T> ResourceResponse<T> successResult(String message, T data , RespCode respCode) {
		return new ResourceResWrapper<T>(true, message, data,respCode);
	}


}
