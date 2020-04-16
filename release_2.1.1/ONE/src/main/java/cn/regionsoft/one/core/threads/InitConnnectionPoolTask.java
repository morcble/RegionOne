package cn.regionsoft.one.core.threads;

import java.util.concurrent.Callable;

import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.SystemContext;
import cn.regionsoft.one.core.dbconnection.CloudDBConnectionPool;
import cn.regionsoft.one.core.dbconnection.ConnectionPool;
import cn.regionsoft.one.core.dbconnection.MongoConnectionPool;
import cn.regionsoft.one.core.dbconnection.SQLConnectionPool;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.DBType;
import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.utils.MongoHelper;

public class InitConnnectionPoolTask implements Callable<ConnectionPool>{
	private static final Logger logger = Logger.getLogger(InitConnnectionPoolTask.class);
	private H2OContext context;
    public InitConnnectionPoolTask(H2OContext context) {
		this.context = context;
	}

	@Override
    public ConnectionPool call(){
    	ConnectionPool connectionPool = null;
    	try {
			if(this.context.getConfig().getDbType()==DBType.MONGODB) {
				connectionPool = new MongoConnectionPool();
				connectionPool.init(context);
			}
			else if(this.context.getConfig().getDbType()==DBType.CLOUDDB) {
				connectionPool = new CloudDBConnectionPool();
				connectionPool.init(context);
			}
			else if(!CommonUtil.isEmpty(this.context.getConfig().getDbType())) {
//				if(SystemContext.CURRENT_SYSTEM_TYPE == SystemType.MOBILE){
//					connectionPool = new ConnectionPoolForMobile();//(ConnectionPool) ConnectionPoolForMobile.class.newInstance();
//				}
//				else{
//					connectionPool = new ConnectionPoolForWeb();//(ConnectionPool) ConnectionPoolForWeb.class.newInstance();
//				}
				connectionPool = new SQLConnectionPool();
				connectionPool.init(context);
			}
		} catch (Exception e) {
			logger.error(e);
		}
        return connectionPool;
    }
}
