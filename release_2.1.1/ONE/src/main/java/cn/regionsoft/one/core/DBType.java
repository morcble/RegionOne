package cn.regionsoft.one.core;


public enum DBType {
	MYSQL("select 1"),
	SQLITE("select 1"),
	ORACLE("select * from dual"),
	//SQLSERVER("select getdate()"),
	MONGODB(""),
	CLOUDDB("");
	
	private String validateQuery;
	private DBType(String validateQuery) {
		this.validateQuery = validateQuery;
	}
	public String getValidateQuery() {
		return validateQuery;
	}
	
}
 