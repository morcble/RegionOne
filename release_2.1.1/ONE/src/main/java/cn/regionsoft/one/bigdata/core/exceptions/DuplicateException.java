package cn.regionsoft.one.bigdata.core.exceptions;


public class DuplicateException extends BizException{
	public DuplicateException(String msg) {
		super(msg);
		errorCode = 400;
	}
}
