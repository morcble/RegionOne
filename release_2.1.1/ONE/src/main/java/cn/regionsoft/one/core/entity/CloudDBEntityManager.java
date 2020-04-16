package cn.regionsoft.one.core.entity;

import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import cn.regionsoft.one.annotation.EntityMappingMode;
import cn.regionsoft.one.annotation.NoInstanceAnoType;
import cn.regionsoft.one.bigdata.core.exceptions.ExistException;
import cn.regionsoft.one.bigdata.core.object.RDSchema;
import cn.regionsoft.one.bigdata.core.object.RDTable;
import cn.regionsoft.one.bigdata.impl.hbase.HbaseThreadHolder;
import cn.regionsoft.one.bigdata.impl.hbase.HbaseUtil;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.ContextConfig;
import cn.regionsoft.one.core.DBType;
import cn.regionsoft.one.core.EntityManager;
import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.core.dbconnection.CloudConnectionManager;
import cn.regionsoft.one.core.dbconnection.CloudDBConnection;
import cn.regionsoft.one.data.dialet.CloudDbDialet;
import cn.regionsoft.one.data.dialet.core.Dialet;
import cn.regionsoft.one.data.persistence.Entity;
import cn.regionsoft.one.properties.ConfigUtil;
import cn.regionsoft.one.rpc.common.ServerConstant;
import cn.regionsoft.one.utils.ThreadData;
import cn.regionsoft.one.utils.ThreadHolder;
import cn.regionsoft.one.utils.TransactionUtil;

public class CloudDBEntityManager extends EntityManager{
	private static final Logger logger = Logger.getLogger(CloudDBEntityManager.class);
	private static boolean showSql = Boolean.valueOf(ConfigUtil.getProperty(ServerConstant.SHOW_SQL));
	private H2OContext h2oContext;
	private Dialet dialet;

	public CloudDBEntityManager(H2OContext h2oContext) {
		this.h2oContext = h2oContext;
	}

	public void init() {
		ContextConfig config = h2oContext.getConfig();
		if(config.getEntityMappingMode() == EntityMappingMode.NONE){
			return;
		}
		dialet = getCurrentDialetIntance(h2oContext);
		
		HashSet<Class<?>> entities= h2oContext.getAnnotatedClassHub().getClassSetByAnnotation(NoInstanceAnoType.Entity);
		if(entities!=null){
			if(config.getEntityMappingMode() == EntityMappingMode.CREATE){
				createMode(entities,dialet);
			}
			else if(config.getEntityMappingMode() == EntityMappingMode.DROP_CREATE){
				dropCreateMode(entities,dialet);
			}
		}
	}
	
	public Dialet getDialet() {
		return dialet;
	}
	
	public static Dialet getCurrentDialetIntance(H2OContext systemContext){
		ContextConfig config = systemContext.getConfig();
		if(config.getDbType()==DBType.CLOUDDB){
			return new CloudDbDialet();
		}
		
		throw new RuntimeException("SystemConfig.DB_TYPE is empty");
	}

	/**
	 * get bindobject by entity class
	 * @param classType
	 * @return
	 */
//	public BindObject getBindObject(Class<?> classType){
//		BindObject result = getEntityToTableCache().get(classType);
//		if(result==null){
//			result = new BindObject(classType);
//			getEntityToTableCache().put(classType, result);
//		}
//		return result;
//	}
	

	
	
	private void createMode(HashSet<Class<?>> entities,Dialet dialet){
		CloudDBConnection connection = null;
		try{
			connection = CloudDBEntityManager.getConnection(h2oContext);

			ContextConfig config = this.h2oContext.getConfig();
			RDSchema rdSchema = HbaseUtil.getRDSchema(config.getAppOwner(),config.getAppId());
			if(rdSchema==null) {
				try {
					rdSchema = HbaseUtil.createRDSchema(config.getAppOwner(),config.getAppId(),config.getSchema());
				}
				catch(ExistException e) {}
			}
			
			SortedMap<String,Class> sortedMap = new TreeMap<String,Class>();
			
			RDTable rdTable = null;
			for(Class<?> tmpClass:entities){
				BindObject bindObject = new BindObject(tmpClass);
				
				rdTable = HbaseUtil.getRdTable(rdSchema, bindObject.getTableName());
				if(rdTable==null) {
					sortedMap.put(tmpClass.getSimpleName(), tmpClass);
					continue;
				}
				
				//更新列
				createColumns(rdTable,bindObject,(CloudDbDialet)dialet);
				h2oContext.getEntityManager().getEntityToTableCache().put(tmpClass, bindObject);
			}
			
			//按照table名排序创建表
			Set<Entry<String, Class>> entry= sortedMap.entrySet();
			for(Entry<String, Class> temp : entry){
				BindObject bindObject = new BindObject(temp.getValue());
				rdTable = HbaseUtil.createRdTable(rdSchema, bindObject.getTableName());
				createColumns(rdTable,bindObject,(CloudDbDialet)dialet);
				h2oContext.getEntityManager().getEntityToTableCache().put(temp.getValue(), bindObject);
			}

			
//			RDTable rdTable = null;
//			for(Class<?> tmpClass:entities){
//				BindObject bindObject = new BindObject(tmpClass);
//				
//				rdTable = HbaseUtil.getRdTable(rdSchema, bindObject.getTableName());
//				if(rdTable==null) {
//					try {
//					rdTable = HbaseUtil.createRdTable(rdSchema, bindObject.getTableName());
//					}
//					catch(ExistException e) {}
//				}
//				
//				createColumns(rdTable,bindObject,(CloudDbDialet)dialet);
//				h2oContext.getEntityManager().getEntityToTableCache().put(tmpClass, bindObject);
//			}
		}
		catch(Exception e){
			logger.error(e);
		}
		finally{
			CloudConnectionManager.releaseConnection(h2oContext);
		}
	}
	
	//增加列
	private void createColumns(RDTable rdTable,BindObject bindObject,CloudDbDialet cloudDbDialet) throws Exception {
		if(bindObject.getIdColumn()!=null){
			try {
				HbaseUtil.createRdColumn(rdTable, bindObject.getIdColumn().getName(),bindObject.getIdColumn().getName(), cloudDbDialet.getDataTypeByClass(bindObject.getIdColumn().getBindType()));
			}
			catch(ExistException e) {}
		}
		
		Map<String,BindColumn> columns = bindObject.getColumns();
		for(BindColumn tmp:columns.values()){
			try {
				HbaseUtil.createRdColumn(rdTable, tmp.getName(),tmp.getName(), cloudDbDialet.getDataTypeByClass(tmp.getBindType()));
			}
			catch(ExistException e) {}
		}
		if(bindObject.getVersionColumn()!=null){
			try {
			HbaseUtil.createRdColumn(rdTable, bindObject.getVersionColumn().getName(), bindObject.getVersionColumn().getName(),cloudDbDialet.getDataTypeByClass(bindObject.getVersionColumn().getBindType()));
			}
			catch(ExistException e) {}
		}
	}
	
	private void dropCreateMode(HashSet<Class<?>> entities,Dialet dialet){
		CloudDBConnection connection = null;
		try{
			connection = CloudDBEntityManager.getConnection(h2oContext);

			ContextConfig config = this.h2oContext.getConfig();
			RDSchema rdSchema = HbaseUtil.getRDSchema(config.getAppOwner(),config.getAppId());
			if(rdSchema==null) {
				try {
					rdSchema = HbaseUtil.createRDSchema(config.getAppOwner(),config.getAppId(),config.getSchema());
				}
				catch(ExistException e) {}
			}
			
			RDTable rdTable = null;
			for(Class<?> tmpClass:entities){
				BindObject bindObject = new BindObject(tmpClass);
				
				HbaseUtil.deleteTable(rdSchema, bindObject.getTableName());
				
				try {
					rdTable = HbaseUtil.createRdTable(rdSchema, bindObject.getTableName());
				}
				catch(ExistException e) {}
				
				createColumns(rdTable,bindObject,(CloudDbDialet)dialet);
				h2oContext.getEntityManager().getEntityToTableCache().put(tmpClass, bindObject);
			}
		}
		catch(Exception e){
			logger.error(e);
		}
		finally{
			CloudConnectionManager.releaseConnection(h2oContext);
		}
	}
	
	
	public static CloudDBConnection getConnection(H2OContext h2oContext) throws Exception{
		CloudDBConnection dbConnection = null;
		ThreadHolder holder = ThreadHolder.getInstance();
		ThreadData threadData = holder.getThreadDatas().get();
		
		if(threadData==null){
			threadData = new ThreadData();
			holder.getThreadDatas().set(threadData);
		}
		
		if(TransactionUtil.isInTransaction(h2oContext)) {
			dbConnection = (CloudDBConnection) threadData.getTransactionConnection(h2oContext);
			
			if(dbConnection==null) {
				dbConnection = (CloudDBConnection) h2oContext.getConnectionFromPool();
				threadData.setTransactionConnection(dbConnection, h2oContext);
				threadData.setTransactionDepth(1, h2oContext);
			}
			else {
				threadData.setTransactionDepth(threadData.getTransactionDepth(h2oContext)+1, h2oContext);
			}
		}
		else {
			dbConnection = (CloudDBConnection) threadData.getNoTrxConnection(h2oContext);	
			
			if(dbConnection==null) {
				dbConnection = (CloudDBConnection) h2oContext.getConnectionFromPool();
				threadData.setNoTrxConnection(dbConnection, h2oContext);
				threadData.setNonTransactionDepth(1, h2oContext);
			}
			else {
				threadData.setNonTransactionDepth(threadData.getNonTransactionDepth(h2oContext)+1, h2oContext);
			}
		}

		//validate connection
		ContextConfig config = h2oContext.getConfig();
		if((System.currentTimeMillis() - dbConnection.getLastUseDt().getTime())>config.getConnectionValidateGap()){
			Statement statement = null;
			try{
				dbConnection.getHbaseDBConnection().testConnectivity();
			}
			catch(Exception e){
				try {
					dbConnection = (CloudDBConnection) h2oContext.newConnection();
					/**
					 * bind connection with thread
					 */
					if(TransactionUtil.isInTransaction(h2oContext)) {
						threadData.setTransactionConnection(dbConnection, h2oContext);
					}
					else {
						threadData.setNoTrxConnection(dbConnection, h2oContext);
					}
				} catch (Exception e1) {
					throw e1;
				}
			}
			finally{
				CommonUtil.closeQuietly(statement);
			}
		}
		
		HbaseThreadHolder.getInstance().setThreadDatas(dbConnection.getHbaseDBConnection());
		return dbConnection;
	}
	
}
