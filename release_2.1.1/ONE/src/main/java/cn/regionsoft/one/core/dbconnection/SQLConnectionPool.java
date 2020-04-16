package cn.regionsoft.one.core.dbconnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.LinkedBlockingQueue;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.ContextConfig;
import cn.regionsoft.one.core.H2OContext;

public class SQLConnectionPool extends AbstractConnectionPoolImpl<SQLConnection> {
	private static final Logger logger = Logger.getLogger(SQLConnectionPool.class);
	
	private H2OContext h2oContext;
	private DataSource jndiDataSource;
	public SQLConnectionPool() {
		super();
	}
	
	@Override
	public void init(H2OContext h2oContext) throws Exception {
		this.h2oContext = h2oContext;
		ContextConfig config = h2oContext.getConfig();
		connections = new LinkedBlockingQueue<SQLConnection>(config.getDbConnPoolSize());
		logger.debug("creating new connections");
        for(int i = 0 ; i <config.getDbConnPoolSize() ; i++){
        	connections.add(newConnection());
		}
        logger.debug("db connections pool is finished");
	}
	
	@Override
	public SQLConnection newConnection() throws Exception{
		logger.debug("creating new connection");
		ContextConfig config = h2oContext.getConfig();
		Connection conn = null;
		if(config.getConnectStr().startsWith("java:comp")){
			if(jndiDataSource==null){
				Context ic = new InitialContext();
				jndiDataSource = (DataSource)ic.lookup(config.getConnectStr()); 
			}
			conn = jndiDataSource.getConnection();  
		}
		else{
			Class.forName(config.getDriver());
			conn = DriverManager.getConnection(config.getConnectStr(),config.getUserName(), config.getPassword());
		}
		return new SQLConnection(conn,h2oContext);
	}
	
	
	
	

}
