package cn.regionsoft.one.web.core;

public enum RespCode {
	_200(200, "OK"),
	_302(302, "REDIRECT"),
	_304(304, "NOT_MODIFIED"),
	_400(400, "BAD_REQUEST"),
	_403(403, "FORBIDDEN"),
	_404(404, "NOT_FOUND"),
	_405(405, "METHOD_NOT_ALLOWED"),
	_500(500, "INTERNAL_SERVER_ERROR"),
	_508(508, "CONTROLLER_ERROR"),
	_503(503, "SERVICE_UNAVAILABLE"),
	_505(505, "FILE_TOO_LARGE"),
	_506(506, "FILE_TYPE_ERROR"), 
	_401(401, "NEED_LOGIN"), 
	_409(409, "CONFLICTION"),
	_700(700, "ValidationError"),
	_701(701, "VerifyCodeError"),
	_702(702, "VerifyCodeExpired")
	;
	
	private int respCode;
	private String reason;

	private RespCode(int respCode, String reason) {
		this.respCode = respCode;
		this.reason = reason;
	}


	public int getRespCode() {
		return respCode;
	}


	public void setRespCode(int respCode) {
		this.respCode = respCode;
	}


	public String getReason() {
		return reason;
	}	
}

/*
▪ 500 Internal Server Error
▪ 501 Not Implemented
▪ 502 Bad Gateway
▪ 503 Service Unavailable
▪ 504 Gateway Timeout
▪ 505 HTTP Version Not Supported
▪ 506 Variant Also Negotiates
▪ 507 Insufficient Storage
▪ 509 Bandwidth Limit Exceeded
▪ 510 Not Extended
▪ 600 Unparseable Response Headers
 */
