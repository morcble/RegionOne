package cn.regionsoft.one.core.dbconnection;

import java.sql.SQLException;
import java.sql.Statement;

import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.ContextConfig;
import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.utils.ThreadData;
import cn.regionsoft.one.utils.ThreadHolder;
import cn.regionsoft.one.utils.TransactionUtil;

public class SQLConnectionManager {
	private static final Logger logger = Logger.getLogger(SQLConnectionManager.class);
//	/**
//	 * Get connection from current thread or connection pool and set the connection owner who are really able to close this connection
//	 * @param systemContext
//	 * @param owner	   : who are really able to close this connection
//	 * @return
//	 */
//	public static DBConnection getConnection(H2OContext h2oContext){
//		ThreadHolder holder = ThreadHolder.getInstance();
//		ThreadData threadData = holder.getThreadDatas().get();
//		if(threadData==null){
//			threadData = new ThreadData();
//			holder.getThreadDatas().set(threadData);
//		}
//		
//		DBConnection dbConnection = threadData.getCurrentConnection(h2oContext);
//		/*
//		 * if no connection bound with current thread ,then try to retrieve a new connection from pool
//		 */
//		if(dbConnection==null){
//			dbConnection = h2oContext.getConnectionFromPool();
//			/**
//			 * bind connection with thread
//			 */
//			threadData.setCurrentConnection(dbConnection,h2oContext);
//		}
//		ContextConfig config = h2oContext.getConfig();
//		/**
//		 * validate connection before use
//		 */
//		if((System.currentTimeMillis() - dbConnection.getLastUseDt().getTime())>config.getConnectionValidateGap()){
//			Statement statement = null;
//			try{
//				statement = dbConnection.createStatement();
//				statement.execute(config.getDbType().getValidateQuery());
//			}
//			catch(Exception e){
//				try {
//					dbConnection = h2oContext.newConnection();
//					/**
//					 * bind connection with thread
//					 */
//					threadData.setCurrentConnection(dbConnection,h2oContext);
//				} catch (Exception e1) {
//					e1.printStackTrace();
//				}
//			}
//			finally{
//				try {statement.close();} catch (SQLException e) {logger.error(e);}
//			}
//		}
//		
//		return dbConnection;
//	}
//	
//	/**
//	 * Please use connection.close(connectionOwner) instead of this
//	 * this method is only used by framework
//	 * @param h2oContext
//	 * @param connectionOwner
//	 */
//	public static void releaseConnection(H2OContext h2oContext){
//		ThreadHolder holder = ThreadHolder.getInstance();
//		ThreadData threadData = holder.getThreadDatas().get();
//		if(threadData!=null){
//			DBConnection dbConnection = threadData.getCurrentConnection(h2oContext);
//			if(dbConnection!=null){
//				/**
//				 * unbind connection with thread
//				 */
//				threadData.setCurrentConnection(null,h2oContext);
//				if(threadData.getTransactionDepth(h2oContext)==1){
//					threadData.setTransactionDepth(0,h2oContext);
//				}
//				/*
//				 *return connection to pool 
//				 */
//				try {
//					dbConnection.setAutoCommit(true);
//				} catch (SQLException e) {
//					logger.error(e);
//				}
//				h2oContext.getConnectionPool().releaseConnectionToPool(dbConnection);
//			}
//		}		
//	}
	/*public static DBConnection getCurrentConnection(H2OContext h2oContext) throws Exception{
		
	}*/
	
	
	public static SQLConnection getCurrentConnection(H2OContext h2oContext) throws Exception{
		SQLConnection dbConnection = null;
		ThreadHolder holder = ThreadHolder.getInstance();
		ThreadData threadData = holder.getThreadDatas().get();
		if(TransactionUtil.isInTransaction(h2oContext)) {
			dbConnection = (SQLConnection) threadData.getTransactionConnection(h2oContext);
		}
		else {
			dbConnection = (SQLConnection) threadData.getNoTrxConnection(h2oContext);
		}
		return dbConnection;
	}
	
	

	/**
	 * used by framework only
	 * @param h2oContext
	 * @return
	 * @throws Exception
	 */
	public static void releaseConnection(H2OContext h2oContext){
		if(h2oContext.getConnectionPool()==null)return;
		
		SQLConnection dbConnection = null;
		ThreadHolder holder = ThreadHolder.getInstance();
		ThreadData threadData = holder.getThreadDatas().get();
		if(TransactionUtil.isInTransaction(h2oContext)) {
			//donothing  transaction的connection由transaction自己释放
			dbConnection = (SQLConnection) threadData.getTransactionConnection(h2oContext);
			
			if(dbConnection!=null) {
				int depth = threadData.getTransactionDepth(h2oContext);
				if(depth<=1){
					threadData.setTransactionDepth(0,h2oContext);
					threadData.setTransactionConnection(null, h2oContext);
					
					try {
						//dbConnection.commit();
						dbConnection.setAutoCommit(true);
					} catch (SQLException e) {
						logger.error(e);
					}
					h2oContext.getConnectionPool().releaseConnectionToPool(dbConnection);
				}
				else {
					threadData.setTransactionDepth(--depth, h2oContext);
				}
			}
		}
		else {
			dbConnection = (SQLConnection) threadData.getNoTrxConnection(h2oContext);
			if(dbConnection!=null) {
				int depth = threadData.getNonTransactionDepth(h2oContext);
				
				if(depth<=1){
					threadData.setNonTransactionDepth(0,h2oContext);
					threadData.setNoTrxConnection(null, h2oContext);
					h2oContext.getConnectionPool().releaseConnectionToPool(dbConnection);
				}
				else {
					threadData.setNonTransactionDepth(--depth, h2oContext);
				}
			}
		}
		
		
	}
}
