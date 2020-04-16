package cn.regionsoft.one.core.auth.dto;

import cn.regionsoft.one.core.auth.LogoutResponseType;

public class LogoutDto {
	private boolean success = false;
	private String account;
	private LogoutResponseType logoutResponseType;
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public LogoutResponseType getLogoutResponseType() {
		return logoutResponseType;
	}
	public void setLogoutResponseType(LogoutResponseType logoutResponseType) {
		this.logoutResponseType = logoutResponseType;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
}
