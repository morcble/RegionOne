package cn.regionsoft.one.core.dbconnection;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.ContextConfig;
import cn.regionsoft.one.core.H2OContext;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoConnectionPool extends AbstractConnectionPoolImpl<MongoConnection> {
	private static final Logger logger = Logger.getLogger(MongoConnectionPool.class);
	
	private H2OContext h2oContext;
	private MongoClient mongoClient;
	@Override
	public void init(H2OContext h2oContext) throws Exception {
		this.h2oContext = h2oContext;
		ContextConfig config = h2oContext.getConfig();
		
		String connectStr = config.getConnectStr();
		String[] tmpArray = connectStr.split(":");

        MongoClientOptions.Builder buider = new MongoClientOptions.Builder();
		buider.connectionsPerHost(config.getDbConnPoolSize());// 最大链接数
        buider.connectTimeout(1000 * 60 * 20);// 连接数据库的超时时间
        buider.maxWaitTime(100 * 60 * 5);//线程成功获取到可用数据库的最大等待时间
        buider.threadsAllowedToBlockForConnectionMultiplier(100);
        buider.maxConnectionIdleTime(0);
        buider.maxConnectionLifeTime(0);
        buider.socketTimeout(0);
        MongoClientOptions myOptions = buider.build();
        
        if(CommonUtil.isEmpty(config.getUserName()) || CommonUtil.isEmpty(config.getPassword())) {
        	mongoClient = new MongoClient(Arrays.asList(
        			new ServerAddress(tmpArray[0], Integer.parseInt(tmpArray[1]))
        		)
        		, myOptions);
        }
        else {
        	MongoCredential credential = MongoCredential.createCredential(config.getUserName(), config.getSchema(),config.getPassword().toCharArray());
        	mongoClient = new MongoClient(Arrays.asList(
        			new ServerAddress(tmpArray[0], Integer.parseInt(tmpArray[1]))
        		)
        		,credential, myOptions);
        }
        
        connections = new LinkedBlockingQueue<MongoConnection>(config.getDbConnPoolSize());
        for(int i = 0 ; i <config.getDbConnPoolSize() ; i++){
        	connections.add(newConnection());
		}
	}

	@Override
	public MongoConnection newConnection() throws Exception {
		return new MongoConnection(mongoClient,this.h2oContext);
	}
	
}
