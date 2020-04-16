package cn.regionsoft.one.core;

import cn.regionsoft.one.core.auth.dto.RequestInfoDto;

public class H2OResourceImpl {
	protected String getMessage(String msgKey){
		RequestInfoDto requestInfoDto = RequestInfoHolder.getInstance().getRequestInfo();
		String localeStr = null;
		if(requestInfoDto!=null) {
			localeStr = requestInfoDto.getLocale();
		}
		
		if(localeStr==null) {
			return I18nMessageManager.getMessage(msgKey);
		}
		else {
			return getMessageByLocale(msgKey,localeStr);
		}
	}
	
	protected String getMessage(String msgKey,String defaultVal){
		String val = getMessage(msgKey,defaultVal);
		if(CommonUtil.isEmpty(val)) {
			return defaultVal;
		}
		else {
			return val;
		}
	}

	private String getMessageByLocale(String msgKey, String locale){
		return I18nMessageManager.getMessage(msgKey, locale);
	}
	
}
