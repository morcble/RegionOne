package cn.regionsoft.one.core;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.regionsoft.one.bigdata.core.object.RDSchema;
import cn.regionsoft.one.bigdata.core.object.RDTable;
import cn.regionsoft.one.bigdata.core.persist.RDEntity;
import cn.regionsoft.one.bigdata.core.persist.RDEntityListResult;
import cn.regionsoft.one.bigdata.core.persist.RDListResultWrapper;
import cn.regionsoft.one.bigdata.criterias.RDCondition;
import cn.regionsoft.one.bigdata.criterias.RDCriteria;
import cn.regionsoft.one.bigdata.impl.hbase.HbaseUtil;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.entity.BindColumn;
import cn.regionsoft.one.core.entity.BindObject;
import cn.regionsoft.one.data.dialet.core.SQLDialet;
import cn.regionsoft.one.data.persistence.H2OEntity;
import cn.regionsoft.one.properties.ConfigUtil;
import cn.regionsoft.one.rpc.common.ServerConstant;
import cn.regionsoft.one.utils.MongoHelper;
import cn.regionsoft.one.utils.TransactionUtil;

/**
 * 
 * @author liangjunf
 *
 * @param <T> entityClass
 * @param <X> primary key
 */
public abstract class CloudH2ODao<T extends H2OEntity,I> implements H2ODaoI<T, I>{
	private static final Logger logger = Logger.getLogger(CloudH2ODao.class);
	
	private static boolean showSql = Boolean.valueOf(ConfigUtil.getProperty(ServerConstant.SHOW_SQL));
	
	@Override
	public List<T> findBySelective(T instance) throws Exception{
		RDListResultWrapper<T> result =  findListWrapperBySelective(instance);
		if(result!=null && result.getList()!=null)return result.getList();
		return null;
	}
	
	/**
	 * 按照非空属性进行查询
	 * @param instance
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public RDListResultWrapper<T> findListWrapperBySelective(T instance) throws Exception{
		Class<T> entityClass = getEntityClass();
		H2OContext h2oContext = CommonUtil.getTargetContext(entityClass);
		BindObject bindObject = h2oContext.getEntityManager().getBindObject(entityClass);
		
		Field field = null;
		Object value = null;
		List<RDCondition> conditionList = new ArrayList<RDCondition>();
		List<BindColumn> columns = bindObject.getAllColumnsExceptVersion();
		for(BindColumn bindColumn:columns) {
			field = bindColumn.getField();
			value = field.get(instance);
			
			if(value==null) continue;
			if(bindColumn.isIdColumn()) {
				T entity = this.find((I) value);
				List<T> result = new ArrayList<T>();
				result.add(entity);
				
				RDListResultWrapper<T> rdListResultWrapper = new RDListResultWrapper<T>();
				rdListResultWrapper.setList(result);
				return rdListResultWrapper;
			}
			
			conditionList.add(RDCondition.equal(bindColumn.getName(),value));
		}
		

		RDCriteria criteria = RDCriteria.create(RDCondition.and(conditionList.toArray(new RDCondition[conditionList.size()])));
		RDListResultWrapper<T> result = getEntityList(criteria);
		return result;	
	}
	
	
	@Override
	public T insert(T instance) throws Exception{
		if(instance==null) throw new RuntimeException("Instance is null");
		H2OContext h2oContext = CommonUtil.getTargetContext(instance.getClass());
		BindObject bindObject = h2oContext.getEntityManager().getBindObject(instance.getClass());
		if(bindObject.getIdColumn()==null) throw new Exception("No Id column defined :"+bindObject.getEntityClass().getName());

		Map<String,Object> newRecord = new LinkedHashMap<String,Object>();
		
		Object value = null;
		Field field = null;
		for(BindColumn bindColumn:bindObject.getColumns().values()){
			field = bindColumn.getField();
			value = field.get(instance);
			newRecord.put(bindColumn.getName(), value);
		}
		
		try{
			RDSchema rdSchema = HbaseUtil.getRDSchema(h2oContext.getConfig().getAppOwner(),h2oContext.getConfig().getAppId());
			RDTable rdTable = HbaseUtil.getRdTable(rdSchema,bindObject.getTableName());
			Long recordId = HbaseUtil.insert(rdTable, newRecord);
			field = bindObject.getIdColumn().getField();
			if(field.getType() == String.class) {
				field.set(instance, String.valueOf(recordId));
			}
			else {
				field.set(instance, recordId);
			}

			return instance;
		}
		catch(Exception e){
			throw e;
		}
		
	}
	

	@Override
	@SuppressWarnings("unchecked")
	public T find(I id) throws Exception{
		if(id==null) throw new RuntimeException("Id is null");

		Class<T> cls = getEntityClass();
		H2OContext h2oContext = CommonUtil.getTargetContext(cls);
		BindObject bindObject = h2oContext.getEntityManager().getBindObject(cls);
		if(bindObject.getIdColumn()==null) throw new Exception("No Id column defined :"+bindObject.getEntityClass().getName());

		//transaction cache begin
		String transactionCacheKey = null;
		if(TransactionUtil.isInTransaction(h2oContext)){
			transactionCacheKey = cls.getName()+"_"+id;
		}
		if(TransactionUtil.transactionCacheConstainKey(h2oContext, transactionCacheKey)){
			return (T) TransactionUtil.getTransactionCache(h2oContext, transactionCacheKey);
		}
		//transaction cache end;
		RDTable rdTable = getRDTable(h2oContext.getConfig().getAppOwner(),h2oContext.getConfig().getAppId(),bindObject.getTableName());
		/*RDSchema rdSchema = HbaseUtil.getRDSchema(h2oContext.getConfig().getAppOwner(),h2oContext.getConfig().getAppId());
		RDTable rdTable = HbaseUtil.getRdTable(rdSchema,bindObject.getTableName());*/
		RDEntity rdEntity = HbaseUtil.findById(rdTable, String.valueOf(id));
		
		T result = (T) RDEntityUtil.transferToEntity(rdEntity, bindObject, cls);
		
		//transaction cache begin
		if(TransactionUtil.isInTransaction(h2oContext)){
			TransactionUtil.addTransactionCache(h2oContext, transactionCacheKey, result);
		}
		//transaction cache end
		return result;
	}
	
	private RDSchema getRDSchema(String owner,String appId) {
		return null;
	}
	
	private static ConcurrentHashMap<String,RDTable> tableMap = new ConcurrentHashMap<String,RDTable>();
	private RDTable getRDTable(String owner,String appId,String tableName) throws Exception {
		String key = owner+appId+tableName;
		RDTable rdTable = tableMap.get(key);
		if(rdTable==null) {
			synchronized(this) {
				rdTable = tableMap.get(key);
				if(rdTable==null) {
					RDSchema rdSchema = HbaseUtil.getRDSchema(owner,appId);
					rdTable = HbaseUtil.getRdTable(rdSchema,tableName);
					tableMap.put(key, rdTable);
				}
			}
		}
		return rdTable;
	}
	

	@SuppressWarnings("unchecked")
	public List<T> findAll() throws Exception{
		Class<T> entityClass = getEntityClass();
		H2OContext h2oContext = CommonUtil.getTargetContext(entityClass);
		BindObject bindObject = h2oContext.getEntityManager().getBindObject(entityClass);
		if(bindObject.getIdColumn()==null) throw new Exception("No Id column defined :"+bindObject.getEntityClass().getName());
		
		RDSchema rdSchema = HbaseUtil.getRDSchema(h2oContext.getConfig().getAppOwner(),h2oContext.getConfig().getAppId());
		RDTable rdTable = HbaseUtil.getRdTable(rdSchema,bindObject.getTableName());
		List<RDEntity> list = HbaseUtil.findAll(rdTable);
		List<T> result = new ArrayList<T>();
		for(RDEntity rdEntity : list) {
			result.add((T) RDEntityUtil.transferToEntity(rdEntity, bindObject, entityClass));
		}
		return result;
	}
	
	//查找被回收的数据
	@SuppressWarnings("unchecked")
	public List<T> findAllRecycledData() throws Exception{
		Class<T> entityClass = getEntityClass();
		H2OContext h2oContext = CommonUtil.getTargetContext(entityClass);
		BindObject bindObject = h2oContext.getEntityManager().getBindObject(entityClass);
		if(bindObject.getIdColumn()==null) throw new Exception("No Id column defined :"+bindObject.getEntityClass().getName());
		
		RDSchema rdSchema = HbaseUtil.getRDSchema(h2oContext.getConfig().getAppOwner(),h2oContext.getConfig().getAppId());
		RDTable rdTable = HbaseUtil.getRdTable(rdSchema,bindObject.getTableName());
		List<RDEntity> list = HbaseUtil.findRecycledData(rdTable);
		List<T> result = new ArrayList<T>();
		for(RDEntity rdEntity : list) {
			result.add((T) RDEntityUtil.transferToEntity(rdEntity, bindObject, entityClass));
		}
		return result;
	}
	
	@Override
	public Integer delete(I id) throws Exception{
		if(id==null) throw new RuntimeException("Id is null");
		Class<T> cls = getEntityClass();
		H2OContext h2oContext = CommonUtil.getTargetContext(cls);
		
		BindObject bindObject = h2oContext.getEntityManager().getBindObject(cls);
		if(bindObject.getIdColumn()==null) throw new Exception("No Id column defined :"+bindObject.getEntityClass().getName());
		
		RDSchema rdSchema = HbaseUtil.getRDSchema(h2oContext.getConfig().getAppOwner(),h2oContext.getConfig().getAppId());
		RDTable rdTable = HbaseUtil.getRdTable(rdSchema,bindObject.getTableName());
		
		HbaseUtil.deleteByRowKey(rdTable, String.valueOf(id));
		
		// transaction cache begin
		String transactionCacheKey = null;
		if (TransactionUtil.isInTransaction(h2oContext)) {
			transactionCacheKey = cls.getName() + "_" + id;
			TransactionUtil.removeTransactionCacheByKey(h2oContext, transactionCacheKey);
		}
		// transaction cache end
		
		return 0;//TODO
	}
	
	@Override
	public Integer softDelete(I id,String operator) throws Exception{
		if(id==null) throw new RuntimeException("Id is null");
		Class<T> cls = getEntityClass();
		H2OContext h2oContext = CommonUtil.getTargetContext(cls);
		
		BindObject bindObject = h2oContext.getEntityManager().getBindObject(cls);
		if(bindObject.getIdColumn()==null) throw new Exception("No Id column defined :"+bindObject.getEntityClass().getName());
		
		RDSchema rdSchema = HbaseUtil.getRDSchema(h2oContext.getConfig().getAppOwner(),h2oContext.getConfig().getAppId());
		RDTable rdTable = HbaseUtil.getRdTable(rdSchema,bindObject.getTableName());
		
		HbaseUtil.moveRecordIntoRecycle(rdTable, String.valueOf(id),operator);
		
		// transaction cache begin
		String transactionCacheKey = null;
		if (TransactionUtil.isInTransaction(h2oContext)) {
			transactionCacheKey = cls.getName() + "_" + id;
			TransactionUtil.removeTransactionCacheByKey(h2oContext, transactionCacheKey);
		}
		// transaction cache end
		return 0;
	}


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

	private T subUpdate(T instance, Map<String,BindColumn> dueToUpDateMap,H2OContext h2oContext,BindObject bindObject) throws Exception{
		if(bindObject.getIdColumn()==null) throw new Exception("No Id column defined :"+bindObject.getEntityClass().getName());
		/**
		 * check id column
		 */
		Field idField = bindObject.getIdColumn().getField();
		@SuppressWarnings("unchecked")
		I idVal = (I) idField.get(instance);
		if(idVal==null) throw new Exception("Primary key value is empty");
		
		RDSchema rdSchema = HbaseUtil.getRDSchema(h2oContext.getConfig().getAppOwner(),h2oContext.getConfig().getAppId());
		RDTable rdTable = HbaseUtil.getRdTable(rdSchema,bindObject.getTableName());
		
		Map<String,Object> updateRecord = new LinkedHashMap<String,Object>();
		Field tmpField = null;
		Object tmpVal = null;
		for(BindColumn bindColumn:dueToUpDateMap.values()){
			tmpField = bindColumn.getField();
			tmpVal = tmpField.get(instance);
			updateRecord.put(bindColumn.getName(), tmpVal);
		}
		
		BindColumn idColumn = bindObject.getIdColumn();
		tmpField = idColumn.getField();
		Object recordId = tmpField.get(instance);
		
		HbaseUtil.updateById(rdTable,String.valueOf(recordId), updateRecord);
	
		// transaction cache begin
		String transactionCacheKey = null;
		if (TransactionUtil.isInTransaction(h2oContext)) {
			transactionCacheKey = instance.getClass().getName() + "_" + idVal.toString();
			TransactionUtil.removeTransactionCacheByKey(h2oContext, transactionCacheKey);
		}
		// transaction cache end
		
		return instance;
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

	//enhancement for entity only------------------------------------------------------------------------------------
	/**
	 */
	protected RDListResultWrapper<T> getEntityList(RDCriteria criteria) throws Exception{
		return getEntityList(criteria,null,null);
	}
	
	/**
	 */
	@SuppressWarnings("unchecked")
	protected RDListResultWrapper<T> getEntityList(RDCriteria criteria, Integer pageNo, Integer pageSize) throws Exception{
		Class<T> entityClass = getEntityClass();
		H2OContext h2oContext = CommonUtil.getTargetContext(entityClass);
		BindObject bindObject = h2oContext.getEntityManager().getBindObject(entityClass);
		RDSchema rdSchema = HbaseUtil.getRDSchema(h2oContext.getConfig().getAppOwner(),h2oContext.getConfig().getAppId());
		RDTable rdTable = HbaseUtil.getRdTable(rdSchema,bindObject.getTableName());
		RDEntityListResult rdListResult = HbaseUtil.getList(rdTable,pageSize,pageNo, criteria);
		
		return RDListResultWrapper.toWrapper(rdListResult, bindObject, entityClass);
	}
	
	protected Long getAmount(RDCriteria criteria) throws Exception{
		Class<T> entityClass = getEntityClass();
		H2OContext h2oContext = CommonUtil.getTargetContext(entityClass);
		BindObject bindObject = h2oContext.getEntityManager().getBindObject(entityClass);
		RDSchema rdSchema = HbaseUtil.getRDSchema(h2oContext.getConfig().getAppOwner(),h2oContext.getConfig().getAppId());
		RDTable rdTable = HbaseUtil.getRdTable(rdSchema,bindObject.getTableName());
		try {
			Long result = HbaseUtil.getAmount(rdTable, criteria);
			return result; 
		} catch (Throwable e) {
			throw new Exception(e);
		}
		
	}
	
	/**
	 * @param preparedSql
	 * @param sqlParas
	 * @return
	 * @throws Exception
	 */
	protected T getEntity(RDCriteria criteria) throws Exception{
		RDListResultWrapper<T> resultWrapper = getEntityList(criteria);
		List<T> ls = resultWrapper.getList();
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
	
	protected String getBindTableName(){
		try{
			EntityManager entityManager = getEntityManager();
			Class<T> entityClass = getEntityClass();
			BindObject bindObject = entityManager.getBindObject(entityClass);
			if(bindObject.getIdColumn()==null) throw new RuntimeException("No Id column defined :"+entityClass.getName());
			String tableName = bindObject.getTableName();
			return tableName;
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
	}

	
	protected H2OContext getDefaultContext() {
		H2OContext h2oContext = SystemContext.getInstance().getContext("DefaultContext");
		return h2oContext;
	}
	
}
