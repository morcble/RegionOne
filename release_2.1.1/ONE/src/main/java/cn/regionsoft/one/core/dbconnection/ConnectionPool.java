package cn.regionsoft.one.core.dbconnection;

import java.sql.Connection;

import cn.regionsoft.one.core.H2OContext;

public interface ConnectionPool {

	void init(H2OContext context) throws Exception;
	
	H2OConnection getConnectionFromPool();

	void releaseConnectionToPool(H2OConnection poolConnection);

	H2OConnection newConnection() throws Exception;

	int avaliable();
}