package cn.regionsoft.one.core.dbconnection;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;

import cn.regionsoft.one.bigdata.impl.hbase.HbaseUtil;
import cn.regionsoft.one.bigdata.core.object.RDSchema;
import cn.regionsoft.one.bigdata.ids.IDProducer;
import cn.regionsoft.one.bigdata.impl.hbase.HbaseDBConnection;
import cn.regionsoft.one.bigdata.impl.hbase.HbaseThreadHolder;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.ContextConfig;
import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.core.ids.CloudIDGenerator;

public class CloudDBConnectionPool extends AbstractConnectionPoolImpl<CloudDBConnection> {
	private static final Logger logger = Logger.getLogger(CloudDBConnectionPool.class);
	
	private H2OContext h2oContext;
	@Override
	public void init(H2OContext h2oContext) throws Exception {
		this.h2oContext = h2oContext;
		ContextConfig config = h2oContext.getConfig();
		
		String dataSpaceName = config.getDataSpaceName();
		String dataRecycleSpaceName = dataSpaceName+"_recycle";
		
		//create dataspace if not exsit
		HbaseUtil.setIDProducer(new IDProducer() {
			@Override
			public Long getNextLongId() {
				try {
					return CloudIDGenerator.getLongID();
				} catch (Exception e) {
					logger.error(e);
					return null;
				}
			}
		});
		
		Connection connection = ConnectionFactory.createConnection(getConfiguration());
		HbaseUtil.createDataSpaceIfNotExist(connection, dataSpaceName);
		HbaseUtil.createDataSpaceIfNotExist(connection, dataRecycleSpaceName);
		HbaseDBConnection hbaseDBConnection = new HbaseDBConnection(connection,getConfiguration(),config.getDataSpaceName());
		
		connections = new LinkedBlockingQueue<CloudDBConnection>(config.getDbConnPoolSize());
		connections.add(new CloudDBConnection(hbaseDBConnection));
        for(int i = 1 ; i <config.getDbConnPoolSize() ; i++){
        	connections.add(newConnection());
		}
	}

	@Override
	public CloudDBConnection newConnection() throws Exception {
		ContextConfig config = h2oContext.getConfig();
		return new CloudDBConnection(new HbaseDBConnection(getConfiguration(),config.getDataSpaceName()));
	}
	
//	private AggregationClient newAggregationClient() {
//		AggregationClient aggregationClient=new AggregationClient(getConfiguration());
//		return aggregationClient;
//	}
//	
	
	private Configuration getConfiguration() {
		ContextConfig config = h2oContext.getConfig();
		Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.property.clientPort", "2181");
        configuration.set("hbase.zookeeper.quorum", "hbase-docker");
        configuration.set("hbase.master", "hbase-docker:60000");
        return configuration;
	}
	

	
}
