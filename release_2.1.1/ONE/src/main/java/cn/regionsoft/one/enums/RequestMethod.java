package cn.regionsoft.one.enums;

public enum RequestMethod {
	GET("GET"),
	POST("POST"),
	PUT("PUT"),
	DELETE("DELETE");
	private RequestMethod(String val){
		this.val = val;
	}
	private String val;
	public String getVal() {
		return val;
	}
	public void setVal(String val) {
		this.val = val;
	}
}
