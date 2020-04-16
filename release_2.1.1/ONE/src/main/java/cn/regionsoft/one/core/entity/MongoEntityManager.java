package cn.regionsoft.one.core.entity;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import cn.regionsoft.one.annotation.EntityMappingMode;
import cn.regionsoft.one.annotation.NoInstanceAnoType;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.ContextConfig;
import cn.regionsoft.one.core.CountResult;
import cn.regionsoft.one.core.DBType;
import cn.regionsoft.one.core.EntityManager;
import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.core.dbconnection.SQLConnection;
import cn.regionsoft.one.core.dbconnection.SQLConnectionManager;
import cn.regionsoft.one.data.dialet.MongoDbDialet;
import cn.regionsoft.one.data.dialet.core.Dialet;
import cn.regionsoft.one.data.dialet.core.SQLDialet;
import cn.regionsoft.one.properties.ConfigUtil;
import cn.regionsoft.one.rpc.common.ServerConstant;

//TODO change  not completed
public class MongoEntityManager extends EntityManager {
	private static final Logger logger = Logger.getLogger(MongoEntityManager.class);
	private static boolean showSql = Boolean.valueOf(ConfigUtil.getProperty(ServerConstant.SHOW_SQL));
	private H2OContext h2oContext;
	private Dialet dialet;

	public MongoEntityManager(H2OContext h2oContext) {
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
		if(config.getDbType()==DBType.MONGODB){
			return new MongoDbDialet();
		}
		
		throw new RuntimeException("SystemConfig.DB_TYPE is empty");
	}

	
	
	private void excuteBatch(List<String> batchSqls) throws Exception{
		SQLConnection connection = SQLEntityManager.getConnection(h2oContext);
		Statement stmt = null;
		try{
			stmt = connection.createStatement();
			int count = 0;
			for(String sql:batchSqls){
				logger.debug(sql);
				stmt.addBatch(sql);
				count++;
				if(count==h2oContext.getConfig().getBatchSize()){
					stmt.executeBatch();
					count = 0;
				}
			}
			
			if(count>0)
				stmt.executeBatch();
			
		}
		catch(Exception e){
			throw e;
		}
		finally{
			CommonUtil.closeQuietly(stmt);
			SQLConnectionManager.releaseConnection(h2oContext);
		}
	
	}
	
	/*public void excuteSql(String sql){
		DBConnection connection = ConnectioManager.getConnection(h2oContext);
		//TODO
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e);
		}
	}*/

	/**
	 * mysql
	 * Return single object by conditions
	 * @param sql
	 * @param resultMapClass
	 * @return
	 * @throws Exception
	 */
	public <T> T getObject(String sql,Object[] sqlParas,Class<T> resultMapClass) throws Exception{
		List<T> ls = getList(sql,sqlParas,resultMapClass);
		if(ls.size()==0){
			return null;
		}
		else if(ls.size()==1){
			return ls.get(0);
		}
		else{
			throw new Exception("More than one records are found");
		}
	}
	
	/**
	 * mysql
	 * Return list data by conditions
	 * @param sql
	 * @param resultMapClass
	 * @return
	 * @throws Exception
	 */
	public <T> List<T> getList(String sql,Object[] sqlParas,Class<T> resultMapClass) throws Exception{
		if(showSql)logger.debug(sql," , paras:",sqlParas);
		SQLConnection connection = SQLEntityManager.getConnection(h2oContext);
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = connection.prepareStatement(sql);
			
			if(sqlParas!=null){
				for(int i = 0 ; i <sqlParas.length ; i++){
					if(sqlParas[i] instanceof java.util.Date){
						if(sqlParas[i]==null){
							ps.setDate(i+1, null);
						}
						else{
							java.util.Date tmpDt = (java.util.Date) sqlParas[i];
							java.sql.Date sqlDt = new java.sql.Date(tmpDt.getTime());
							ps.setDate(i+1, sqlDt);
						}
					}
					else if(sqlParas[i] instanceof Boolean){
						ps.setBoolean(i+1, (Boolean) sqlParas[i]);
						//ps.setInt(i+1, (boolean) sqlParas[i]?1:0);
					}
					else{
						ps.setObject(i+1, sqlParas[i]);
					}
				}
			}
			
			rs = ps.executeQuery();
			List<T> ls = CommonUtil.resolveResultSet(rs,resultMapClass, h2oContext);
			return ls;
		}
		catch(Exception e){
			throw e;
		}
		finally{
			CommonUtil.closeQuietly(rs);
			CommonUtil.closeQuietly(ps);
			
			SQLConnectionManager.releaseConnection(h2oContext);
		}
	}
	
	
	
	
	private void createMode(HashSet<Class<?>> entities,Dialet dialet){
		if(dialet instanceof SQLDialet) {
			SQLDialet sqlDialet = (SQLDialet) dialet;
			SQLConnection connection = null;
			try{
				connection = SQLEntityManager.getConnection(h2oContext);
				List<String> sqls = new ArrayList<String>();
				for(Class<?> tmpClass:entities){
					BindObject bindObject = new BindObject(tmpClass);
					h2oContext.getEntityManager().getEntityToTableCache().put(tmpClass, bindObject);
					String sql = sqlDialet.getTableExsitsQuery(bindObject.getTableName(),h2oContext.getConfig().getSchema());
					
					CountResult countResult = getObject(sql,null,CountResult.class);
					/**
					 * Create table if not exists
					 */
					if(countResult.getCount()==0){
						sqls.add(sqlDialet.getCreateTableSql(bindObject));
					}
				}
				
				try{
					if(Logger.specialLogEnabled){
						for(String sql:sqls){
							if(showSql)logger.debug(sql);
						}
					}
					connection.setAutoCommit(false);
					excuteBatch(sqls);
					connection.commit();
				}
				catch(Exception e){
					throw e;
				}	
			}
			catch(Exception e){
				logger.error(e);
			}
			finally{
				try {
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				SQLConnectionManager.releaseConnection(h2oContext);
			}
		}
	}
	
	private void dropCreateMode(HashSet<Class<?>> entities,Dialet dialet){
		if(dialet instanceof SQLDialet) {
			SQLDialet sqlDialet = (SQLDialet) dialet;
			SQLConnection connection = null;
			try{
				connection = SQLEntityManager.getConnection(h2oContext);
				List<String> sqls = new ArrayList<String>();
				for(Class<?> tmpClass:entities){
					BindObject bindObject = new BindObject(tmpClass);
					h2oContext.getEntityManager().getEntityToTableCache().put(tmpClass, bindObject);
					String sql = sqlDialet.getTableExsitsQuery(bindObject.getTableName(),h2oContext.getConfig().getSchema());
					
					CountResult countResult = getObject(sql,null,CountResult.class);
					/**
					 * Create table if not exists
					 */
					if(countResult.getCount()==0){
						sqls.add(sqlDialet.getCreateTableSql(bindObject));
					}
					else{
						sqls.add(sqlDialet.getDropTableSql(bindObject.getTableName()));
						sqls.add(sqlDialet.getCreateTableSql(bindObject));
					}
				}
				
				try{
					connection.setAutoCommit(false);
					excuteBatch(sqls);
					connection.commit();
				}
				catch(Exception e){
					throw e;
				}
			}
			catch(Exception e){
				logger.error(e);
			}
			finally{
				try {
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				SQLConnectionManager.releaseConnection(h2oContext);
			}
		}
		
	}
	
	
	
	
}
