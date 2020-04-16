package cn.regionsoft.one.core;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import cn.regionsoft.one.annotation.EntityMappingMode;
import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.core.aop.AOPListener;
import cn.regionsoft.one.properties.ConfigUtil;
import com.cnautosoft.silver.client.PropertiesManager;

public class SYSEnvSetup {
	private static volatile boolean inited = false;
	
	public static synchronized SystemContext setUp() throws Exception{
		if(inited) {//如果已经初始化
			return SystemContext.getInstance();
		}
		else {
			synchronized(SYSEnvSetup.class) {
				if(inited) {//如果已经初始化
					return SystemContext.getInstance();
				}
				else {
					SystemContext systemContext = initSystemContext();
					inited = true;
					return systemContext;
				}
			}
		}	
	}
	
	private static SystemContext initSystemContext() throws Exception {
		Properties props = new Properties();
		InputStream input = SYSEnvSetup.class.getClassLoader().getResourceAsStream("config.properties");
		try {
			if(input!=null)props.load(input);
		} catch (Exception e) {
			throw e;
		}
		finally{
			if(input!=null)input.close();
		}
		
		ConfigUtil.setProps(props);
		String logpath = ConfigUtil.getProperty("logpath");
		if(logpath!=null) {
			System.setProperty("logpath", logpath);
		}
		else {
			System.setProperty("logpath", "");
		}
		
		
		if(!CommonUtil.isEmpty(ConfigUtil.getProperty(Constants.SILVER_GROUP_ID))) {
			PropertiesManager pm = PropertiesManager.getInstance();
			pm.init((String) ConfigUtil.getProperty(Constants.SILVER_HOST,"silver.host"),
					Integer.parseInt((String) ConfigUtil.getProperty(Constants.SILVER_PORT,"9003")),
					(String) ConfigUtil.getProperty(Constants.SILVER_GROUP_ID),
					(String) ConfigUtil.getProperty(Constants.SILVER_ITEM_ID));
			
			ConfigUtil.setSilverConfigExsit(true);
		}

		
		Constants.SYSTEM_ROOT = ConfigUtil.getProperty("serverroot");
		Constants.FILE_SERVER_ROOT=ConfigUtil.getProperty("fileroot");
		
		 /**
		  * init system environment variable
		  */
		
		if(Constants.FILE_SERVER_ROOT!=null) {
			File tmp = new File(Constants.FILE_SERVER_ROOT);
			if(!tmp.exists()){
				tmp.mkdirs();
			}
			tmp = new File(Constants.FILE_SERVER_ROOT+Constants.SYSTEM_SEPERATOR+"tmp");
			if(!tmp.exists()){
				tmp.mkdirs();
			}
		}

		SystemContext systemContext =  SystemContext.getInstance();
		String contexts = ConfigUtil.getProperty("contexts");
		if(CommonUtil.isEmpty(contexts)){
			contexts = "DefaultContext";
		}
		String[] contextArray = contexts.split(",");
		if(contextArray.length==0){
			throw new RuntimeException("No Context found");
		}
		
		ContextConfig[] contextConfigArray = new ContextConfig[contextArray.length];
		
		for(int i = 0 ; i <contextArray.length ; i++){
			contextConfigArray[i] = initContextWithProperties(contextArray[i]);
		}
		systemContext.init(contextConfigArray);
		
		/**
		 * init resources.i18n message begin
		 */
		I18nMessageManager.loadI18nMessage();
		
		/**
		 * init resources.i18n message end
		 */
		return systemContext;
	}
	
	public static void releaseResource(){
		//SystemContext.getInstance().releaseResources();
	}
	
	private static ContextConfig initContextWithProperties(String contextName) throws Exception{
		//TODO Load from remote silver
		//InputStream fileIs = SYSEnvSetup.class.getClassLoader().getResourceAsStream(contextName+".properties");
		
		//Properties properties = CommonUtil.loadProperties(fileIs);
		ContextConfig contextConfig = new ContextConfig();
		/*String contextName = ConfigUtil.getProperty(CONTEXT_NAME);
		if(CommonUtil.isEmpty(contextName)){
			contextConfig.setContextName(SystemContext.DEFAULT_CONTEXT);
		}
		else{
			contextConfig.setContextName(ConfigUtil.getProperty(CONTEXT_NAME));
		}*/
		contextConfig.setContextName(contextName);
		
		contextConfig.setDbType(CommonUtil.getEnumByName(ConfigUtil.getProperty(contextName+DB_TYPE), DBType.class));
		contextConfig.setUserName(ConfigUtil.getProperty(contextName+USER_NAME));
		contextConfig.setPassword(ConfigUtil.getProperty(contextName+PASSWORD));
		contextConfig.setSchema(ConfigUtil.getProperty(contextName+SCHEMA));
		
		if(contextConfig.getDbType()==DBType.MONGODB) {
			contextConfig.setDbConnPoolSize(Integer.parseInt(ConfigUtil.getProperty(contextName+DB_CONNPOOL_SIZE)));
			contextConfig.setConnectStr(ConfigUtil.getPropertyAndPromiseNotNull(contextName+CONNECTSTR));
			
			contextConfig.setConnectionValidateGap(Long.parseLong(ConfigUtil.getPropertyAndPromiseNotNull(contextName+CONNECTION_VALIDATE_GAP)));
		}
		else if(contextConfig.getDbType()==DBType.CLOUDDB) {
			//donothing
			contextConfig.setDbConnPoolSize(Integer.parseInt(ConfigUtil.getProperty(contextName+DB_CONNPOOL_SIZE)));
			contextConfig.setDataSpaceName(ConfigUtil.getPropertyAndPromiseNotNull(contextName+DATA_SPACE));
			contextConfig.setAppOwner(ConfigUtil.getPropertyAndPromiseNotNull(contextName+APP_OWNER));
			contextConfig.setAppId(ConfigUtil.getPropertyAndPromiseNotNull(contextName+APP_UNIQUE_NAME));
			contextConfig.setProgrameId(ConfigUtil.getPropertyAndPromiseNotNull(contextName+PROGRAME_ID));
			
			contextConfig.setConnectionValidateGap(Long.parseLong(ConfigUtil.getPropertyAndPromiseNotNull(contextName+CONNECTION_VALIDATE_GAP)));
			
			contextConfig.setEntityMappingMode(CommonUtil.getEnumByName(ConfigUtil.getProperty(contextName+ENTITY_MAPPING_MODE,"NONE"), EntityMappingMode.class));
		}
		else if(!CommonUtil.isEmpty(contextConfig.getDbType())){//MYSQL,ORACLE,SQLITE,SQLSERVER
			contextConfig.setDbConnPoolSize(Integer.parseInt(ConfigUtil.getProperty(contextName+DB_CONNPOOL_SIZE)));
			contextConfig.setDriver(ConfigUtil.getPropertyAndPromiseNotNull(contextName+DRIVER));
			contextConfig.setConnectStr(ConfigUtil.getPropertyAndPromiseNotNull(contextName+CONNECTSTR));
			contextConfig.setBatchSize(Integer.parseInt(ConfigUtil.getPropertyAndPromiseNotNull(contextName+BATCH_SIZE)));
			contextConfig.setConnectionValidateGap(Long.parseLong(ConfigUtil.getPropertyAndPromiseNotNull(contextName+CONNECTION_VALIDATE_GAP)));
			
			contextConfig.setEntityMappingMode(CommonUtil.getEnumByName(ConfigUtil.getProperty(contextName+ENTITY_MAPPING_MODE,"NONE"), EntityMappingMode.class));
		}
		
		String systemContextPathsConf = ConfigUtil.getPropertyAndPromiseNotEmpty(contextName+SYSTEM_CONTEXT_PATHS);
		contextConfig.setSystemContextPaths(systemContextPathsConf.split(","));
		
		
		String aopListenersStr = ConfigUtil.getProperty(contextName+AOP_LISTENERS);
		if(!CommonUtil.isEmpty(aopListenersStr)){
			String[] tmpArray = aopListenersStr.split(",");
			AOPListener[] aopListeners = new AOPListener[tmpArray.length];
			for(int i = 0 ; i <tmpArray.length ; i++){
				Object t = Class.forName(tmpArray[i]).newInstance();
				if(!(t instanceof AOPListener)){
					throw new RuntimeException(tmpArray[i]+" is not a valid aop listener");
				}
				aopListeners[i] = (AOPListener) t;
			}
			contextConfig.setAopListeners(aopListeners);
		}

		
		/**
		 * for mobile only
		 */
		if(contextConfig.getDbType()==DBType.SQLITE) {
			contextConfig.setMobileSqlitePwdstring(ConfigUtil.getProperty(contextName+MOBILE_SQLITE_PWDSTRING));
			contextConfig.setMobileSqliteDBFile(ConfigUtil.getProperty(contextName+MOBILE_SQLITE_DBFILE));
		}
		return contextConfig;
	}
	/**
	 * constants for configuration
	 */
	
	private static String DB_CONNPOOL_SIZE = ".db.poolsize";
	private static String DB_TYPE = ".db.type";
	private static String DRIVER = ".db.driver";
	private static String USER_NAME = ".db.account";
	private static String PASSWORD = ".db.password";
	
	private static String CONNECTSTR=".db.connectstr";
	private static String SCHEMA=".db.schema";
	private static String BATCH_SIZE=".db.batchsize";
	private static String CONNECTION_VALIDATE_GAP=".db.connection.validateinterval";
	private static String DATA_SPACE=".db.dataspace";
	private static String APP_OWNER=".appOwner";
	private static String APP_UNIQUE_NAME=".appUniqueName";
	private static String PROGRAME_ID=".programeId";
	
	private static String SYSTEM_CONTEXT_PATHS=".context.paths";
	private static String ENTITY_MAPPING_MODE=".entity.mode";
	
	private static String AOP_LISTENERS=".aoplisteners";
	
	/**
	 * for mobile version
	 */
	private static String MOBILE_SQLITE_PWDSTRING="mobileSqlitePwdstring";
	private static String MOBILE_SQLITE_DBFILE="mobileSqliteDBFile";
	
	

}
