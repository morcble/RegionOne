package cn.regionsoft.one.bigdata.core.exceptions;

public class BizException extends Exception{
	private static final long serialVersionUID = 1484586715302983624L;
	public BizException(String msg){
		super(msg);
	}
	public BizException(String message, Throwable cause) {
        super(message, cause);
    }
    public BizException(Throwable cause) {
        super(cause);
    }
    
    protected int errorCode = 500;
	
	public int getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

}
