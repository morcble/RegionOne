package cn.regionsoft.one.core.auth;

import java.text.SimpleDateFormat;
import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.RequestInfoHolder;
import cn.regionsoft.one.core.auth.dto.RequestInfoDto;

public class AuthUtil {

	public static String getCurrentUserAccount() {
		String account = null;
		RequestInfoDto requestInfoDto = RequestInfoHolder.getInstance().getRequestInfo();
		if(requestInfoDto!=null){
			account = requestInfoDto.getLoginAccount();
		}
		if(account==null)return "Guest";
		else return account;
	}
	
	public static SimpleDateFormat getDateFormater() {
		return CommonUtil.getSimpleDateFormat(Constants.DATE_FORMAT1);
	}
	
	public static SimpleDateFormat getDateFormater(String formatPattern) {
		return CommonUtil.getSimpleDateFormat(formatPattern);
	}

}
