package cn.regionsoft.one.bigdata.core.exceptions;


public class ExistException extends BizException{
	public ExistException(String msg) {
		super(msg);
		errorCode = 400;
	}
}
