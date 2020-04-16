package cn.regionsoft.one.core.auth;

public enum LoginResponseType {
	INVALID_REQUEST,
	INVALID_ACCOUNT,
	INVALID_PASSWORD,
	ACCOUNT_LOCKED,
	LOGIN_SUCCESSFULLY,
	INVALID_VERIFYIMG,
	EXPIRED_VERIFYIMG,
}
