package cn.regionsoft.one.core.aop.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.sql.SQLException;

import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.DBType;
import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.core.aop.AnnotationListener;
import cn.regionsoft.one.core.dbconnection.H2OConnection;
import cn.regionsoft.one.core.dbconnection.SQLConnection;
import cn.regionsoft.one.core.dbconnection.SQLConnectionManager;
import cn.regionsoft.one.core.entity.SQLEntityManager;
import cn.regionsoft.one.data.persistence.Transactional;
import cn.regionsoft.one.utils.ThreadData;
import cn.regionsoft.one.utils.ThreadHolder;
import cn.regionsoft.one.utils.TransactionUtil;

public class TransactionListener implements AnnotationListener{
	private static final Logger logger = Logger.getLogger(TransactionListener.class);
	
	public Class<? extends Annotation> targetAnnotation(){
		return Transactional.class;
	}
	
	public Boolean beforeInvoke(Object obj, java.lang.reflect.Method method, Object[] args,H2OContext h2oContext){
		try{
			if(h2oContext.getConfig().getDbType()==DBType.MONGODB) {
				
			}
			else {
				beginSQLTransaction(h2oContext);
			}
			
			return null;
    	}
    	catch(Throwable ex){
    		logger.error(ex);
    		return null;
    	}
    	finally{
    		
    	}
	}
	
	public Object afterInvoke(Object obj, java.lang.reflect.Method method, Object[] args, Object result,H2OContext h2oContext){
		try{
			if(h2oContext.getConfig().getDbType()==DBType.MONGODB) {
				
			}
			else {
				commitSQLTransaction(h2oContext);
			}
    		return null;
    	}
    	catch(Throwable ex){
    		logger.error(ex);
    		return null;
    	}

	}

	@Override
	public void exceptionInvoke(Throwable e,H2OContext h2oContext) {
		try{
			if(h2oContext.getConfig().getDbType()==DBType.MONGODB) {
				
			}
			else {
				rollbackSQLTransaction(h2oContext);
			}
    	}
    	catch(Throwable ex){
    		logger.error(ex);
    	}

	}

	@Override
	public void finalInvoke(Object obj, Method method, Object[] args, Object result,
			H2OContext h2oContext) {
		try{
			if(h2oContext.getConfig().getDbType()==DBType.MONGODB) {
				
			}
			else {
				releaseSQLTransactionResource(h2oContext);
			}
    	}
    	catch(Throwable ex){
    		logger.error(ex);
    	}
	}
	
	private void beginSQLTransaction(H2OContext context) throws SQLException{
		try {
			if(!TransactionUtil.isInTransaction(context)) {
				ThreadHolder holder = ThreadHolder.getInstance();
				ThreadData threadData = holder.getThreadDatas().get();
				threadData.setTransactionDepth(1, context);
			}
			
			SQLConnection dbConnection = SQLEntityManager.getConnection(context);
			dbConnection.setAutoCommit(false);
		} catch (Exception e) {
			logger.error(e);
			throw new SQLException(e);
		}
	}
	
	private void commitSQLTransaction(H2OContext context) throws Exception{
		ThreadHolder holder = ThreadHolder.getInstance();
		ThreadData threadData = holder.getThreadDatas().get();
		
		if(threadData.getTransactionDepth(context)==1){
			H2OConnection dbConnection = threadData.getTransactionConnection(context);
			dbConnection.commit();
		}
	}
	
	private void rollbackSQLTransaction(H2OContext context) throws Exception{
		ThreadHolder holder = ThreadHolder.getInstance();
		ThreadData threadData = holder.getThreadDatas().get();
		

		if(threadData.getTransactionDepth(context)==1){
			H2OConnection dbConnection = threadData.getTransactionConnection(context);
			dbConnection.rollback();
		}
	}
	
	private void releaseSQLTransactionResource(H2OContext context) {
		SQLConnectionManager.releaseConnection(context);
		
		TransactionUtil.clearTransactionCache(context);
	}

}
