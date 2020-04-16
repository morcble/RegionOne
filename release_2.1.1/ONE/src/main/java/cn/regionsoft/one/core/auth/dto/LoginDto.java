package cn.regionsoft.one.core.auth.dto;

import cn.regionsoft.one.core.auth.LoginResponseType;

public class LoginDto {
	private boolean isValidAccount = false;
	private String sessionId = null;
	private String userInfoJson = null;
	private String token = null;
	private LoginResponseType loginResponseType = null;
	
	public boolean isValidAccount() {
		return isValidAccount;
	}

	public void setValidAccount(boolean isValidAccount) {
		this.isValidAccount = isValidAccount;
	}

	public String getUserInfoJson() {
		return userInfoJson;
	}

	public void setUserInfoJson(String userInfoJson) {
		this.userInfoJson = userInfoJson;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public LoginResponseType getLoginResponseType() {
		return loginResponseType;
	}

	public void setLoginResponseType(LoginResponseType loginResponseType) {
		this.loginResponseType = loginResponseType;
	}
}
