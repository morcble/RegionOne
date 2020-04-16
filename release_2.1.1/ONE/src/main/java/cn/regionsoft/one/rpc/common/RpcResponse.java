package cn.regionsoft.one.rpc.common;

import java.io.Serializable;

import io.protostuff.Tag;

/**
 * 
 * @author fenglj
 *
 */
public class RpcResponse implements Serializable{
	private String requestId;
	private Object result;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }



    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
