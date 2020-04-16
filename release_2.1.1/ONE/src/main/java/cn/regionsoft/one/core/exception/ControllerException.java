package cn.regionsoft.one.core.exception;

public class ControllerException extends Exception{
	private static final long serialVersionUID = 1484586715302983624L;
	public ControllerException(String msg){
		super(msg);
	}
	public ControllerException(String message, Throwable cause) {
        super(message, cause);
    }
    public ControllerException(Throwable cause) {
        super(cause);
    }

}
