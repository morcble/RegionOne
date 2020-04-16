package cn.regionsoft.one.core;

import cn.regionsoft.one.annotation.EntityMappingMode;
import cn.regionsoft.one.core.aop.AOPListener;

public class ContextConfig {
	/**
	 * context name
	 */
	private String contextName;
	/**
	 * size of db connection pool
	 */
	private int dbConnPoolSize = 0;
	/**
	 * db type
	 */
	private DBType dbType;
	/**
	 * schema
	 */
	private String schema;
	/**
	 * jdbc driver
	 */
	private String driver;
	/**
	 * db username 
	 */
	private String userName;
	/**
	 * db password
	 */
	private String password;
	/**
	 * db connection string
	 */
	private String connectStr;
	/**
	 * the max limit of batch size
	 */
	private int batchSize;
	/**
	 * indicate the interval system validate the db connection
	 */
	private Long connectionValidateGap;
	/**
	 * single xframe context path
	 */
	private String[] systemContextPaths;
	/**
	 * entity mapping mode for auto create or drop tables
	 */
	private EntityMappingMode entityMappingMode;
	/**
	 * all the aop listeners been registered in system
	 */
	private AOPListener[] aopListeners;
	
	/**
	 * for mobile encrypt sqllite only
	 */
	private String mobileSqlitePwdstring;
	/**
	 * for mobile sqllite only
	 */
	private String mobileSqliteDBFile;
	
	public String getMobileSqliteDBFile() {
		return mobileSqliteDBFile;
	}
	public void setMobileSqliteDBFile(String mobileSqliteDBFile) {
		this.mobileSqliteDBFile = mobileSqliteDBFile;
	}
	public String getMobileSqlitePwdstring() {
		return mobileSqlitePwdstring;
	}
	public void setMobileSqlitePwdstring(String mobileSqlitePwdstring) {
		this.mobileSqlitePwdstring = mobileSqlitePwdstring;
	}
	public String[] getSystemContextPaths() {
		return systemContextPaths;
	}
	public void setSystemContextPaths(String[] systemContextPaths) {
		this.systemContextPaths = systemContextPaths;
	}
	
	public EntityMappingMode getEntityMappingMode() {
		return entityMappingMode;
	}
	public void setEntityMappingMode(EntityMappingMode entityMappingMode) {
		this.entityMappingMode = entityMappingMode;
	}
	public AOPListener[] getAopListeners() {
		return aopListeners;
	}
	public void setAopListeners(AOPListener[] aopListeners) {
		this.aopListeners = aopListeners;
	}
	public int getDbConnPoolSize() {
		return dbConnPoolSize;
	}
	public void setDbConnPoolSize(int dbConnPoolSize) {
		this.dbConnPoolSize = dbConnPoolSize;
	}
	public DBType getDbType() {
		return dbType;
	}
	public void setDbType(DBType dbType) {
		this.dbType = dbType;
	}
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getConnectStr() {
		return connectStr;
	}
	public void setConnectStr(String connectStr) {
		this.connectStr = connectStr;
	}
	public int getBatchSize() {
		return batchSize;
	}
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	public Long getConnectionValidateGap() {
		return connectionValidateGap;
	}
	public void setConnectionValidateGap(Long connectionValidateGap) {
		this.connectionValidateGap = connectionValidateGap;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public String getContextName() {
		return contextName;
	}
	public void setContextName(String contextName) {
		this.contextName = contextName;
	}
	
	//clouddb only
	private String dataSpaceName;
	private String appOwner;
	private String appId;
	private String programeId;
	public void setDataSpaceName(String dataSpaceName) {
		this.dataSpaceName = dataSpaceName;
		
	}
	public String getDataSpaceName() {
		return dataSpaceName;
	}
	public String getAppOwner() {
		return appOwner;
	}
	public void setAppOwner(String appOwner) {
		this.appOwner = appOwner;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getProgrameId() {
		return programeId;
	}
	public void setProgrameId(String programeId) {
		this.programeId = programeId;
	}
	
	public boolean hasDbConnection() {
		if(CommonUtil.isEmpty(this.getDbType())){
			return false;
		}
		else return true;
	}
}
