package cn.regionsoft.one.core.auth.dto;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Locale;

import cn.regionsoft.one.bigdata.core.object.RDSchema;
import cn.regionsoft.one.common.Constants;

public class RequestInfoDto implements Serializable{
	private static final long serialVersionUID = 4814282565600273621L;
	private String token = null;
	private String loginAccount = null;
	
	private String requestId = null;
	/**
	 * international languages
	 */
	private String locale = null;
	
	private SimpleDateFormat simpleDateFormat;
	
	
	//for cloud app begin
	private String rsAppId;
	private String regionName;
	//for cloud app end
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getLoginAccount() {
		return loginAccount;
	}
	public void setLoginAccount(String loginAccount) {
		this.loginAccount = loginAccount;
	}
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	public String getRequestId() {
		return requestId;
	}
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}
	
	public String getRsAppId() {
		return rsAppId;
	}
	public void setRsAppId(String rsAppId) {
		this.rsAppId = rsAppId;
	}
	public String getRegionName() {
		return regionName;
	}
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	
}
