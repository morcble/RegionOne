package cn.regionsoft.one.core.dbconnection;

import java.sql.Statement;

import cn.regionsoft.one.bigdata.impl.hbase.HbaseThreadHolder;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.ContextConfig;
import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.utils.ThreadData;
import cn.regionsoft.one.utils.ThreadHolder;
import cn.regionsoft.one.utils.TransactionUtil;

public class CloudConnectionManager {
	private static final Logger logger = Logger.getLogger(CloudConnectionManager.class);
	
	/**
	 * used by dao
	 * @param h2oContext
	 * @return
	 * @throws Exception
	 */
	public static CloudDBConnection getCurrentConnection(H2OContext h2oContext) throws Exception{
		CloudDBConnection dbConnection = null;
		ThreadHolder holder = ThreadHolder.getInstance();
		ThreadData threadData = holder.getThreadDatas().get();
		if(TransactionUtil.isInTransaction(h2oContext)) {
			dbConnection = (CloudDBConnection) threadData.getTransactionConnection(h2oContext);
		}
		else {
			dbConnection = (CloudDBConnection) threadData.getNoTrxConnection(h2oContext);
		}
		return dbConnection;
	}
	

	/*
	 * used by framework only
	 * @param h2oContext
	 */
	public static void releaseConnection(H2OContext h2oContext){
		CloudDBConnection dbConnection = null;
		ThreadHolder holder = ThreadHolder.getInstance();
		ThreadData threadData = holder.getThreadDatas().get();
		if(TransactionUtil.isInTransaction(h2oContext)) {
			//donothing  transaction的connection由transaction自己释放
			dbConnection = (CloudDBConnection) threadData.getTransactionConnection(h2oContext);
			
			if(dbConnection!=null) {
				int depth = threadData.getTransactionDepth(h2oContext);
				if(depth<=1){
					threadData.setTransactionDepth(0,h2oContext);
					threadData.setTransactionConnection(null, h2oContext);
					
					try {
						//dbConnection.commit();
						dbConnection.setAutoCommit(true);
					} catch (Exception e) {
						logger.error(e);
					}
					h2oContext.getConnectionPool().releaseConnectionToPool(dbConnection);
					HbaseThreadHolder.getInstance().setThreadDatas(null);
				}
				else {
					threadData.setTransactionDepth(--depth, h2oContext);
				}
			}
		}
		else {
			dbConnection = (CloudDBConnection) threadData.getNoTrxConnection(h2oContext);
			if(dbConnection!=null) {
				int depth = threadData.getNonTransactionDepth(h2oContext);
				
				if(depth<=1){
					threadData.setNonTransactionDepth(0,h2oContext);
					threadData.setNoTrxConnection(null, h2oContext);
					h2oContext.getConnectionPool().releaseConnectionToPool(dbConnection);
					HbaseThreadHolder.getInstance().setThreadDatas(null);
				}
				else {
					threadData.setNonTransactionDepth(--depth, h2oContext);
				}
			}
		}
		
		
	}
}
