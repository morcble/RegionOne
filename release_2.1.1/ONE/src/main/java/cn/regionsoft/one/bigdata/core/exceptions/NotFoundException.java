package cn.regionsoft.one.bigdata.core.exceptions;


public class NotFoundException extends BizException{
	public NotFoundException(String msg) {
		super(msg);
		errorCode = 400;
	}
}
