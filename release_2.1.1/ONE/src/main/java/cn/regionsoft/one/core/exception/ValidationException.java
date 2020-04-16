package cn.regionsoft.one.core.exception;

public class ValidationException extends Exception{
	private static final long serialVersionUID = 1484586715302983624L;
	public ValidationException(String msg){
		super(msg);
	}
	public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    public ValidationException(Throwable cause) {
        super(cause);
    }

}
