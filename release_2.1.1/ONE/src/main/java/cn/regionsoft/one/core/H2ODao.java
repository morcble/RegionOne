package cn.regionsoft.one.core;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bson.Document;

import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.dbconnection.SQLConnection;
import cn.regionsoft.one.core.dbconnection.SQLConnectionManager;
import cn.regionsoft.one.core.entity.BindColumn;
import cn.regionsoft.one.core.entity.BindObject;
import cn.regionsoft.one.core.entity.SQLEntityManager;
import cn.regionsoft.one.data.dialet.core.SQLDialet;
import cn.regionsoft.one.data.persistence.H2OEntity;
import cn.regionsoft.one.data.persistence.criteria.Condition;
import cn.regionsoft.one.data.persistence.criteria.Operator;
import cn.regionsoft.one.data.persistence.criteria.Query;
import cn.regionsoft.one.properties.ConfigUtil;
import cn.regionsoft.one.rpc.common.ServerConstant;
import cn.regionsoft.one.utils.MongoHelper;
import cn.regionsoft.one.utils.TransactionUtil;
import com.mongodb.client.MongoCollection;

/**
 * 
 * @author liangjunf
 *
 * @param <T> entityClass
 * @param <X> primary key
 */
public abstract class H2ODao<T extends H2OEntity,I> implements H2ODaoI<T, I>{
	private static final Logger logger = Logger.getLogger(H2ODao.class);
	
	private static boolean showSql = Boolean.valueOf(ConfigUtil.getProperty(ServerConstant.SHOW_SQL));

	/**
	 * 按照非空属性进行查询
	 * @param instance
	 * @return
	 * @throws Exception
	 */
	@Override
	public List<T> findBySelective(T instance) throws Exception{
		Class<T> entityClass = getEntityClass();
		H2OContext h2oContext = CommonUtil.getTargetContext(entityClass);
		BindObject bindObject = h2oContext.getEntityManager().getBindObject(entityClass);
		
		if(h2oContext.getConfig().getDbType()==DBType.MONGODB) {
			MongoCollection<Document> collection = MongoHelper.getBindCollection(bindObject.getEntityClass());
			
			Query query = new Query();
			
			List<BindColumn> columns = bindObject.getAllColumnsExceptVersion();
			Field field = null;
			Object value = null;
			for(BindColumn bindColumn:columns) {
				field = bindColumn.getField();
				field.setAccessible(true);
				value = field.get(instance);
				if(value!=null) {
					query.addCondition(new Condition(bindColumn.getName(),Operator.EQ,value));
				}
			}
			
			return MongoHelper.getList(entityClass, collection, query);
		}
		else {
			SQLConnection connection = SQLConnectionManager.getCurrentConnection(h2oContext);
			PreparedStatement ps = null;
			try{
				String tableName = bindObject.getTableName();
				
				StringBuilder sqlBuf = new StringBuilder("select * from ");
				sqlBuf.append(tableName);
				sqlBuf.append(" where 1=1 ");
				
				List<BindColumn> columns = bindObject.getAllColumnsExceptVersion();
				Field field = null;
				Object value = null;
				Map<BindColumn,Object> paras = new LinkedHashMap<BindColumn,Object>();
				for(BindColumn bindColumn:columns) {
					field = bindColumn.getField();
					field.setAccessible(true);
					value = field.get(instance);
					if(value!=null) {
						sqlBuf.append(" and "+bindColumn.getName()+"= ?");
						paras.put(bindColumn,value);
					}
				}

				ps = connection.prepareStatement(sqlBuf.toString());
			
				Iterator<Entry<BindColumn,Object>> iterator = paras.entrySet().iterator();
				Entry<BindColumn,Object> tmpEntry = null;
				BindColumn bindColumn = null;
				int index = 0;
				while(iterator.hasNext()){
					tmpEntry = iterator.next();
					bindColumn = tmpEntry.getKey();
					value = tmpEntry.getValue();
			
					if(bindColumn.getBindType() == java.util.Date.class){
						if(value==null){
							ps.setDate(++index, null);
						}
						else{
							java.util.Date utilDate = (java.util.Date) value;
							java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
							ps.setTimestamp(++index, sqlDate);
						}
					}
					else if(bindColumn.getBindType() == Boolean.class){
						ps.setBoolean(++index, (Boolean) value);
					}
					else{
						ps.setObject(++index, value);
					}
				}
				if(showSql)logger.debug(sqlBuf.toString()+" :"+paras.values());
				ResultSet rs = ps.executeQuery();
				return CommonUtil.resolveResultSet(rs,entityClass, h2oContext);
			}
			catch(Exception e){
				throw e;
			}
			finally{
				CommonUtil.closeQuietly(ps);
			}
		}
	}
	
	
	/**
	 * mysql & mongo
	 */
	@Override
	public T insert(T instance) throws Exception{
		if(instance==null) throw new RuntimeException("Instance is null");
		H2OContext h2oContext = CommonUtil.getTargetContext(instance.getClass());
		BindObject bindObject = h2oContext.getEntityManager().getBindObject(instance.getClass());
		if(bindObject.getIdColumn()==null) throw new Exception("No Id column defined :"+bindObject.getEntityClass().getName());
		
		if(h2oContext.getConfig().getDbType()==DBType.MONGODB) {
			MongoHelper.insert(instance);
			return instance;
		}
		
		SQLDialet dialet = (SQLDialet) h2oContext.getEntityManager().getDialet();
		String preparedSql = dialet.getInsertSql(bindObject);
		if(showSql)logger.debug(preparedSql+" :"+instance);
		SQLConnection connection = SQLConnectionManager.getCurrentConnection(h2oContext);
		PreparedStatement pst = null;
		try{
			pst = connection.prepareStatement(preparedSql);
			List<BindColumn> columns = bindObject.getAllColumnsExceptVersion();
			setPrepareStm(pst, columns,instance,bindObject.getVersionColumn());
			pst.execute();

			return instance;
		}
		catch(Exception e){
			throw e;
		}
		finally{
			CommonUtil.closeQuietly(pst);
		}
	}
	
	/**
	 * mysql & mongo
	 */
	@Override
	public T find(I id) throws Exception{
		if(id==null) throw new RuntimeException("Id is null");
		Class<T> cls = getEntityClass();
		H2OContext h2oContext = CommonUtil.getTargetContext(cls);
		
		BindObject bindObject = h2oContext.getEntityManager().getBindObject(cls);
		if(bindObject.getIdColumn()==null) throw new Exception("No Id column defined :"+bindObject.getEntityClass().getName());
		
		
		if(h2oContext.getConfig().getDbType()==DBType.MONGODB) {
			T instance = MongoHelper.find(id,cls);
			return instance;
		}
		
		//transaction cache begin
		String transactionCacheKey = null;
		if(TransactionUtil.isInTransaction(h2oContext)){
			transactionCacheKey = cls.getName()+Constants.UNDER_LINE+id;
		}
		if(TransactionUtil.transactionCacheConstainKey(h2oContext, transactionCacheKey)){
			return (T) TransactionUtil.getTransactionCache(h2oContext, transactionCacheKey);
		}
		//transaction cache end
		
		SQLDialet dialet = (SQLDialet) h2oContext.getEntityManager().getDialet();
		
		String sql = dialet.getFindSql(bindObject);
		SQLConnection connection = SQLConnectionManager.getCurrentConnection(h2oContext);
		T result = getObject(sql,new Object[]{id},cls,h2oContext);
		
		//transaction cache begin
		if(TransactionUtil.isInTransaction(h2oContext)){
			TransactionUtil.addTransactionCache(h2oContext, transactionCacheKey, result);
		}
		//transaction cache end
		return result;
	}
	
	/**
	 * mysql & mongo
	 */
	public List<T> findAll() throws Exception{
		Class<T> cls = getEntityClass();
		H2OContext h2oContext = CommonUtil.getTargetContext(cls);
		BindObject bindObject = h2oContext.getEntityManager().getBindObject(cls);
		if(bindObject.getIdColumn()==null) throw new Exception("No Id column defined :"+bindObject.getEntityClass().getName());
		
		if(h2oContext.getConfig().getDbType()==DBType.MONGODB) {
			return MongoHelper.findAll(cls);
		}
		else {
			SQLDialet dialet = (SQLDialet) h2oContext.getEntityManager().getDialet();
			String sql = dialet.getFindAllSql(bindObject);
			return getList(sql,null,cls,h2oContext);
		}
	}
	
	/**
	 * mysql & mongo
	 */
	@Override
	public Integer delete(I id) throws Exception{
		if(id==null) throw new RuntimeException("Id is null");
		Class<T> cls = getEntityClass();
		H2OContext h2oContext = CommonUtil.getTargetContext(cls);
		
		if(h2oContext.getConfig().getDbType()==DBType.MONGODB) {
			MongoHelper.delete(id,cls);
		}
		else {
			BindObject bindObject = h2oContext.getEntityManager().getBindObject(cls);
			if(bindObject.getIdColumn()==null) throw new Exception("No Id column defined :"+bindObject.getEntityClass().getName());
			SQLDialet dialet = (SQLDialet) h2oContext.getEntityManager().getDialet();
			
			String sql = dialet.getDeleteSql(bindObject);
			SQLConnection connection = SQLConnectionManager.getCurrentConnection(h2oContext);
			excute(sql,new Object[]{id},connection);
			// transaction cache begin
			String transactionCacheKey = null;
			if (TransactionUtil.isInTransaction(h2oContext)) {
				transactionCacheKey = cls.getName() + Constants.UNDER_LINE + id;
				TransactionUtil.removeTransactionCacheByKey(h2oContext, transactionCacheKey);
			}
			// transaction cache end
		}
		
		return 0;//TODO
	}
	

	@Override
	public Integer softDelete(I id,String operator) throws Exception {
		// TODO Auto-generated method stub
		//CALL update
		Class<T> entityClass = getEntityClass();
		H2OContext h2oContext = CommonUtil.getTargetContext(entityClass);
		BindObject bindObject = h2oContext.getEntityManager().getBindObject(entityClass);
		
		int result = 0;
		if(h2oContext.getConfig().getDbType()==DBType.MONGODB) {
			MongoCollection<Document> collection = MongoHelper.getBindCollection(bindObject.getEntityClass());
			//TODO
		}
		else {
			SQLConnection connection = SQLConnectionManager.getCurrentConnection(h2oContext);
			PreparedStatement ps = null;
			try{
				String tableName = bindObject.getTableName();
				
				StringBuilder sqlBuf = new StringBuilder("update ");
				sqlBuf.append(tableName);
				sqlBuf.append(" set softDelete=1 where id=? and softDelete = 0");

				ps = connection.prepareStatement(sqlBuf.toString());
				ps.setObject(1, id);
				
				if(showSql)logger.debug(sqlBuf.toString()+" :"+id);
				result = ps.executeUpdate();
				

				// transaction cache begin
				String transactionCacheKey = null;
				if (TransactionUtil.isInTransaction(h2oContext)) {
					transactionCacheKey = entityClass.getName() + Constants.UNDER_LINE + String.valueOf(id);
					TransactionUtil.removeTransactionCacheByKey(h2oContext, transactionCacheKey);
				}
				// transaction cache end
				
			}
			catch(Exception e){
				throw e;
			}
			finally{
				CommonUtil.closeQuietly(ps);
			}
		}
		return result;
	}

	/**
	 * mysql & mongo
	 */
	@Override
	public T update(T instance) throws Exception{
		H2OContext h2oContext = CommonUtil.getTargetContext(instance.getClass());
		BindObject bindObject = h2oContext.getEntityManager().getBindObject(instance.getClass());

		Map<String,BindColumn> dueToUpDateMap = new LinkedHashMap<String,BindColumn>();
		Collection<BindColumn> collection= bindObject.getColumns().values();
		for(BindColumn tmp:collection){
			dueToUpDateMap.put(tmp.getName(), tmp);
		}
		subUpdate(instance,dueToUpDateMap,h2oContext,bindObject);
		return instance;
	}

	/**
	 * mysql & mongo
	 */
	@Override
	public T update(T instance, String... columns) throws Exception{
		HashSet<String> set = new HashSet<String>();
		for(String tmp:columns){
			set.add(tmp);
		}
		return update(instance,set);
	}

	/**
	 * mysql & mongo
	 */
	@Override
	public T update(T instance, Set<String> columns) throws Exception{
		H2OContext h2oContext = CommonUtil.getTargetContext(instance.getClass());
		BindObject bindObject = h2oContext.getEntityManager().getBindObject(instance.getClass());
		
		Map<String,BindColumn> dueToUpDateMap = new LinkedHashMap<String,BindColumn>();
		Collection<BindColumn> collection= bindObject.getColumns().values();
		for(BindColumn tmp:collection){
			if(columns.contains(tmp.getName())){
				dueToUpDateMap.put(tmp.getName(), tmp);
				columns.remove(tmp.getName());
			}
		}
		for(String tmp:columns){
			throw new Exception("Update exception :column "+ tmp + " is not found ,"+instance.getClass().getName());
		}
		return subUpdate(instance,dueToUpDateMap,h2oContext,bindObject);
	}
	
	/**
	 * mysql & mongo
	 */
	private T subUpdate(T instance, Map<String,BindColumn> dueToUpDateMap,H2OContext h2oContext,BindObject bindObject) throws Exception{
		if(bindObject.getIdColumn()==null) throw new Exception("No Id column defined :"+bindObject.getEntityClass().getName());

		/**
		 * check id column
		 */
		Field idField = bindObject.getIdColumn().getField();
		idField.setAccessible(true);
		@SuppressWarnings("unchecked")
		I idVal = (I) idField.get(instance);
		if(idVal==null) throw new Exception("Primary key value is empty");
		
		/**
		 * check version column,  optimistic lock
		 */
		int paraLength = dueToUpDateMap.size()+1;
		if(bindObject.getVersionColumn()!=null){
			paraLength++;
			/*T old = find(idVal);
			if(old==null) throw new Exception("Record is not found with primary key = "+idVal + " in class "+bindObject.getEntityClass().getName());
			
			Field versionField = bindObject.getVersionColumn().getField();
			versionField.setAccessible(true);
			Integer oldVersion = (Integer) versionField.get(old);
			Integer currVersion = (Integer) versionField.get(instance);
			if(currVersion==null) throw new Exception("Version check failed, version column is required");
			if(oldVersion.intValue()!=currVersion.intValue()) {
				logger.error("version confiliction! old version:"+oldVersion.intValue()+", current version:"+currVersion.intValue());
				throw new Exception("Optimistic lock check failed, Data is out of date");
			}
			
			versionField.set(instance, currVersion+1);*/
		}
		
		if(h2oContext.getConfig().getDbType()==DBType.MONGODB) {
			if(bindObject.getVersionColumn()!=null){
				dueToUpDateMap.put(bindObject.getVersionColumn().getName(), bindObject.getVersionColumn());
			}
			MongoHelper.update(idVal,instance,dueToUpDateMap);

			return instance;
		}
		else {
			SQLDialet dialet = (SQLDialet) h2oContext.getEntityManager().getDialet();
		
			String sql = dialet.getUpdateSql(bindObject,dueToUpDateMap);
			SQLConnection connection = SQLConnectionManager.getCurrentConnection(h2oContext);
			
			Object[] sqlParas = new Object[paraLength];
			int index = 0;
			Field tmpField = null;
			Object tmpVal = null;
			for(BindColumn bindColumn:dueToUpDateMap.values()){
				tmpField = bindColumn.getField();
				tmpVal = tmpField.get(instance);
				sqlParas[index] = tmpVal;
				index++;
			}
			
			if(bindObject.getVersionColumn()!=null){
				tmpField = bindObject.getVersionColumn().getField();
				tmpVal = tmpField.get(instance);
				sqlParas[index] = tmpVal;
				index++;
			}
			
			tmpField = bindObject.getIdColumn().getField();
			tmpVal = tmpField.get(instance);
			sqlParas[index] = tmpVal;

			
			excute(sql, sqlParas,connection);
			// transaction cache begin
			String transactionCacheKey = null;
			if (TransactionUtil.isInTransaction(h2oContext)) {
				transactionCacheKey = instance.getClass().getName() + Constants.UNDER_LINE + idVal.toString();
				TransactionUtil.removeTransactionCacheByKey(h2oContext, transactionCacheKey);
			}
			// transaction cache end
			
			return instance;
		}
	}

	/**
	 * mysql
	 * @param preparedSql
	 * @param sqlParas
	 * @param connection
	 * @throws Exception
	 */
	private void excute(String preparedSql,Object[] sqlParas,SQLConnection connection) throws Exception{
		if(showSql)logger.debug(preparedSql," , paras:",sqlParas);
		PreparedStatement ps = null;
		try {
			ps = connection.prepareStatement(preparedSql);
			
			if(sqlParas!=null){
				for(int i = 0 ; i <sqlParas.length ; i++){
					if(sqlParas[i] instanceof java.util.Date){
						if(sqlParas[i]==null){
							ps.setDate(i+1, null);
						}
						else{
							java.util.Date tmpDt = (java.util.Date) sqlParas[i];
							java.sql.Timestamp sqlDt = new java.sql.Timestamp(tmpDt.getTime());
							ps.setTimestamp(i+1, sqlDt);
						}
					}
					else if(sqlParas[i] instanceof Boolean){
						ps.setBoolean(i+1, (Boolean) sqlParas[i]);
					}
					else{
						ps.setObject(i+1, sqlParas[i]);
					}
				}
			}
			ps.execute();
		}
		catch(Exception e){
			throw e;
		}
		finally{
			CommonUtil.closeQuietly(ps);
		}
	}
	

	/**
	 * 
	 * @return
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	protected Class<T> getEntityClass() throws ClassNotFoundException{
		Class<?> orignalClass = null;
		String proxyClassName = this.getClass().getName();
		int index = proxyClassName.indexOf("$$");
		if(index == -1) orignalClass =  this.getClass();
		else orignalClass = Class.forName(proxyClassName.substring(0,index));
		
		Type[] types = ((ParameterizedType)orignalClass.getGenericSuperclass()).getActualTypeArguments();
		if(types.length!=0){
			return (Class<T>) types[0];
		}
		else return null;
	}
	
	/**
	 * mysql
	 * Return single object by conditions
	 * @param sql
	 * @param resultMapClass
	 * @return
	 * @throws Exception
	 */
	private T getObject(String preparedSql,Object[] sqlParas,Class<T> resultMapClass,H2OContext h2oContext) throws Exception{
		List<T> ls = getList(preparedSql,sqlParas,resultMapClass,h2oContext);
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
	protected  List<T> getList(String preparedSql,Object[] sqlParas,Class<T> resultMapClass ,H2OContext h2oContext) throws Exception{
		return (List<T>) this.getGenericList(preparedSql, sqlParas, resultMapClass, h2oContext);
	}
	
	protected  List<?> getGenericList(String preparedSql,Object[] sqlParas,Class<?> resultMapClass ,H2OContext h2oContext) throws Exception{
		if(showSql)logger.debug(preparedSql," , paras:",sqlParas);
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			SQLConnection connection = SQLConnectionManager.getCurrentConnection(h2oContext);
			ps = connection.prepareStatement(preparedSql);
			
			if(sqlParas!=null){
				for(int i = 0 ; i <sqlParas.length ; i++){
					if(sqlParas[i] instanceof java.util.Date){
						if(sqlParas[i]==null){
							ps.setDate(i+1, null);
						}
						else{
							java.util.Date tmpDt = (java.util.Date) sqlParas[i];
							java.sql.Timestamp sqlDt = new java.sql.Timestamp(tmpDt.getTime());
							ps.setTimestamp(i+1, sqlDt);
						}
					}
					else if(sqlParas[i] instanceof Boolean){
						ps.setBoolean(i+1, (Boolean) sqlParas[i]);
					}
					else{
						ps.setObject(i+1, sqlParas[i]);
					}
				}
			}
			
			rs = ps.executeQuery();
			List<?> ls = CommonUtil.resolveResultSet(rs,resultMapClass, h2oContext);
			return ls;
		}
		catch(Exception e){
			throw e;
		}
		finally{
			CommonUtil.closeQuietly(rs);
			CommonUtil.closeQuietly(ps);
		}
	}
	

	public List<T> getListByIds(Long[] ids) throws Exception{
		String tableName = getBindTableName();
		
		StringBuilder sqlBuf = new StringBuilder("select * from ");
		sqlBuf.append(tableName);
		sqlBuf.append(" where id in (");
		for(int i = 0 ; i<ids.length ; i++) {
			sqlBuf.append(ids[i]);
			if(i!=ids.length-1)
				sqlBuf.append(",");
		}
		sqlBuf.append(")");
		
		return getEntityList(sqlBuf.toString());
	}
	
	/**
	 * common static methods 
	 * @param pst
	 * @param columns
	 * @param instance
	 * @throws Exception
	 */
	private static void setPrepareStm(PreparedStatement pst ,List<BindColumn> columns, Object instance,BindColumn versionColumn) throws Exception{
		int index = 0;
		Field field = null;
		Object value = null;
		for(BindColumn bindColumn:columns){
			field = bindColumn.getField();
			field.setAccessible(true);
			value = field.get(instance);
	
			if(bindColumn.getBindType() == java.util.Date.class){
				if(value==null){
					pst.setDate(++index, null);
				}
				else{
					java.util.Date utilDate = (java.util.Date) value;
					java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
					pst.setTimestamp(++index, sqlDate);
				}
			}
			else if(bindColumn.getBindType() == Boolean.class){
				pst.setBoolean(++index, (Boolean) value);
			}
			else{
				pst.setObject(++index, value);
			}
		}
		
		
		if(versionColumn!=null){
			field = versionColumn.getField();
			field.setAccessible(true);
			Integer versionNo = (Integer) field.get(instance);
			
			if(versionNo==null) versionNo = 0;
			else versionNo = versionNo+1;
			
			field.set(instance, versionNo);
			pst.setInt(++index, versionNo);
		}
	}
	
	//enhancement for entity only------------------------------------------------------------------------------------
	/**
	 * mysql
	 * @param preparedSql
	 * @param sqlParas
	 * @return
	 * @throws Exception
	 */
	protected List<T> getEntityList(String preparedSql,Object[] sqlParas) throws Exception{
		return getEntityList(preparedSql,sqlParas,null,null);
	}
	
	protected List<T> getEntityList(String preparedSql) throws Exception{
		return getEntityList(preparedSql,null,null,null);
	}
	
	//获取普通结果的list
	protected <G> List<G> getGenericList(String preparedSql,Object[] sqlParas,Class<G> resultClass,Integer pageNo, Integer pageSize) throws Exception{
		Class<T> entityClass = getEntityClass();
		H2OContext h2oContext = CommonUtil.getTargetContext(entityClass);
		SQLDialet dialet = (SQLDialet) h2oContext.getEntityManager().getDialet();
		
		preparedSql = dialet.getPagenationQuery(preparedSql,pageNo, pageSize);		
		return (List<G>) getGenericList(preparedSql,sqlParas,resultClass ,h2oContext);
	}
	
	protected <G> List<G> getGenericList(String preparedSql,Object[] sqlParas,Class<G> resultClass) throws Exception{
		return getGenericList(preparedSql, sqlParas, resultClass, null, null);
	}
	
	protected <G> List<G> getGenericList(String preparedSql,Class<G> resultClass) throws Exception{
		return getGenericList(preparedSql, null, resultClass, null, null);
	}
	
	/**
	 * mysql
	 * @param preparedSql
	 * @param sqlParas
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */
	protected List<T> getEntityList(String preparedSql,Object[] sqlParas, Integer pageNo, Integer pageSize) throws Exception{
		Class<T> entityClass = getEntityClass();
		H2OContext h2oContext = CommonUtil.getTargetContext(entityClass);
		SQLDialet dialet = (SQLDialet) h2oContext.getEntityManager().getDialet();
		
		preparedSql = dialet.getPagenationQuery(preparedSql,pageNo, pageSize);		
		return getList(preparedSql,sqlParas,entityClass ,h2oContext);
	}
	
	/**
	 * mysql
	 * @param preparedSql
	 * @param sqlParas
	 * @return
	 * @throws Exception
	 */
	protected T getEntity(String preparedSql,Object[] sqlParas) throws Exception{
		List<T> ls = getEntityList(preparedSql,sqlParas);
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
	
	protected EntityManager getEntityManager(){
		try {
			Class<T> entityClass = getEntityClass();
			return CommonUtil.getEntityManager(entityClass);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * for mysql
	 * @return
	 */
	protected String getBindTableName(){
		try{
			Class<T> entityClass = getEntityClass();
			H2OContext h2oContext = CommonUtil.getTargetContext(entityClass);
			BindObject bindObject = h2oContext.getEntityManager().getBindObject(entityClass);
			if(bindObject.getIdColumn()==null) throw new RuntimeException("No Id column defined :"+entityClass.getName());
			String tableName = bindObject.getTableName();
			return tableName;
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * for mongo db
	 * @return
	 */
	protected MongoCollection<Document> getBindCollection(){
		try{
			Class<?> entityClass = getEntityClass();
			return MongoHelper.getBindCollection(entityClass);
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * mysql
	 * @param preparedSql
	 * @param sqlParas
	 * @throws Exception
	 */
	protected void excute(String preparedSql,Object[] sqlParas)throws Exception{
		Class<T> entityClass = getEntityClass();
		H2OContext h2oContext = CommonUtil.getTargetContext(entityClass);
		SQLConnection connection = SQLConnectionManager.getCurrentConnection(h2oContext);
		excute(preparedSql, sqlParas,connection);
	}
	
	
	protected H2OContext getDefaultContext() {
		H2OContext h2oContext = SystemContext.getInstance().getContext("DefaultContext");
		return h2oContext;
	}
	
}
