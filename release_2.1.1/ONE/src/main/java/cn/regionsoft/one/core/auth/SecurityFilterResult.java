package cn.regionsoft.one.core.auth;

import cn.regionsoft.one.enums.UserToDoAction;

public class SecurityFilterResult {
	private UserToDoAction userToDoAction ; 
	private String account;
	private String token;
	public SecurityFilterResult(UserToDoAction userToDoAction, String account,String token) {
		super();
		this.userToDoAction = userToDoAction;
		this.account = account;
		this.token = token;
	}
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
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
}
