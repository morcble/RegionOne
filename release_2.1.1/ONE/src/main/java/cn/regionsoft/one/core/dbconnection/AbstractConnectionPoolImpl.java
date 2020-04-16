package cn.regionsoft.one.core.dbconnection;

import java.sql.Connection;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.properties.ConfigUtil;

public abstract class AbstractConnectionPoolImpl<T extends H2OConnection> implements ConnectionPool{
	private static final Logger logger = Logger.getLogger(AbstractConnectionPoolImpl.class);
	protected BlockingQueue<T> connections = new LinkedBlockingQueue<T>(100);
	public T getConnectionFromPool(){
		try {
			T connection = connections.take();
			//logger.debug("get connection,"+connections.size());
			return connection;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error(e);
			return null;
		}
		finally {
			//logger.debug("getConnectionFromPool,current avaliable connections:"+avaliable());
		}
	}
	
	@Override
	public int avaliable(){
		return connections.size();
	}
	
	@Override
	public void releaseConnectionToPool(H2OConnection poolConnection) {
		try {
			H2OConnection tmp = ((H2OConnection)poolConnection);
			tmp.setLastUseDt(new Date());
			connections.add((T) poolConnection);
			//logger.debug("release connection,"+connections.size());
		}
		finally {
			//logger.debug("releaseConnectionToPool,current avaliable connections:"+avaliable());
		}
	}
}
