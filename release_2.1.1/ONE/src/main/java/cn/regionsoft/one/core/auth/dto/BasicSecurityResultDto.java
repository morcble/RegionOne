package cn.regionsoft.one.core.auth.dto;

import cn.regionsoft.one.enums.UserToDoAction;

public class BasicSecurityResultDto {
	private UserToDoAction userToDoAction;
	private String account;
	private String loginId;
	public UserToDoAction getUserToDoAction() {
		return userToDoAction;
	}
	public void setUserToDoAction(UserToDoAction userToDoAction) {
		this.userToDoAction = userToDoAction;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getLoginId() {
		return loginId;
	}
	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}
}
