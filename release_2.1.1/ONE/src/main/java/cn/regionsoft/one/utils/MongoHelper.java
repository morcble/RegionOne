package cn.regionsoft.one.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.bson.BsonArray;
import org.bson.Document;
import org.bson.types.ObjectId;

import cn.regionsoft.one.common.JsonUtil;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.ContextConfig;
import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.core.dbconnection.MongoConnection;
import cn.regionsoft.one.core.dbconnection.MongoConnectionPool;
import cn.regionsoft.one.core.entity.BindColumn;
import cn.regionsoft.one.core.entity.BindObject;
import cn.regionsoft.one.core.entity.SQLEntityManager;
import cn.regionsoft.one.data.persistence.Entity;
import cn.regionsoft.one.data.persistence.criteria.Condition;
import cn.regionsoft.one.data.persistence.criteria.Operator;
import cn.regionsoft.one.data.persistence.criteria.Query;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public class MongoHelper {
	private static final String MONGO_ID="_id";
	private static final String ID="id";
	
	private static final Logger logger = Logger.getLogger(MongoHelper.class);
	
	public static MongoCollection<Document> getCollectionByEntity(MongoConnection mongoConnection ,Class entityClass) {
		String simpleName = entityClass.getSimpleName();
		return null;//mongoDatabase.getCollection(simpleName.substring(0, simpleName.length()-6));
	}
	

	public static <T> List<T> findAll(Class<T> entityClass) {
		MongoCollection<Document> collection = getBindCollection(entityClass);
		
		FindIterable<Document> iterables = collection.find();
		MongoCursor<Document> cursor = iterables.iterator();
		
		List<T> result = new ArrayList<T>();
	    while (cursor.hasNext()) {  
	    	Document tmp = cursor.next();
	    	T entity = documentToEntity(tmp,entityClass);
	    	result.add(entity); 
	    }
		return result;
	}
	
	public static <T> void insert(T instance) {
		MongoCollection<Document> collection = getBindCollection(instance.getClass());
		collection.insertOne(entityToDocument(instance));
	}
	
	/**
	 * find record by id
	 * @param id
	 * @param resultClass
	 * @return
	 */
	public static <T,I> T find(I id ,Class<T> resultClass) {
		MongoCollection<Document> collection = getBindCollection(resultClass);
		Query query = new Query();
		query.addCondition(new Condition(MONGO_ID,Operator.EQ,id));
		
		List<T> ls = getList(resultClass ,collection ,query);
		if(ls==null||ls.size()==0) {
			return null;
		}
		else {
			return ls.get(0);
		}
	}
	
	public static <T,I> void delete(I id, Class<T> entityClass) {
		MongoCollection<Document> collection = getBindCollection(entityClass);
		BasicDBObject filter = new BasicDBObject(); 
		filter.put(MONGO_ID,id);
		collection.deleteOne(filter);
	}
	
	public static <T,I> void update(I id, T instance, Map<String, BindColumn> dueToUpdateMap) throws IllegalArgumentException, IllegalAccessException {
		MongoCollection<Document> collection = getBindCollection(instance.getClass());
		BasicDBObject filter = new BasicDBObject(); 
		filter.put(MONGO_ID,id);
		
		Document document = new Document();
		Field tmpField = null;
		Object tmpVal = null;
		for(BindColumn bindColumn:dueToUpdateMap.values()){
			tmpField = bindColumn.getField();
			tmpVal = tmpField.get(instance);
			document.put(bindColumn.getName(), tmpVal);
		}

		collection.updateOne(filter,new Document(SET,document));
	}
	
	/**
	 * 
	 * @param entityClass
	 * @param filtersValues  过滤条件		 列名-值
	 * @param newValues   需要更新的值  列名-值
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static <T,I> void updateMany(Class entityClass, Map<String, Object> filtersValues,Map<String, Object> newValues) throws IllegalArgumentException, IllegalAccessException {
		MongoCollection<Document> collection = getBindCollection(entityClass);
		BasicDBObject filter = new BasicDBObject(); 
		Iterator<Entry<String, Object>> iterator = filtersValues.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, Object> entry = iterator.next();
			filter.put(entry.getKey(),entry.getValue());
		}
		
		Document document = new Document();
		iterator = newValues.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, Object> entry = iterator.next();
			document.put(entry.getKey(),entry.getValue());
		}
		collection.updateMany(filter,new Document(SET,document));
	}
	
	public static MongoCollection<Document> getBindCollection(Class entityClass){
		MongoConnection connection = null;
		try{
			H2OContext h2oContext = CommonUtil.getTargetContext(entityClass);
			BindObject bindObject = h2oContext.getEntityManager().getBindObject(entityClass);
			connection = (MongoConnection) h2oContext.getConnectionFromPool();
			ContextConfig config = h2oContext.getConfig();
			MongoDatabase mongoDatabase = connection.getMongoClient().getDatabase(config.getSchema());
			MongoCollection<Document> collection = mongoDatabase.getCollection(bindObject.getTableName());
			return collection;
		}
		catch(Exception e){
			throw new RuntimeException(e);
		}
		finally {
			CommonUtil.closeQuietly(connection);
		}
	}
	
	/**
	 * document 转 对象
	 * @param document
	 * @param classType
	 * @return
	 */
	public static <T> T documentToEntity(Document document,Class<T> classType) {
		try {
			H2OContext h2oContext = CommonUtil.getTargetContext(classType);
			T instance = classType.newInstance();
			Field field = null;
			BindObject bindObject = h2oContext.getEntityManager().getBindObject(instance.getClass());
			
			if(bindObject.getVersionColumn()!=null) {
				field = bindObject.getVersionColumn().getField();
				field.setAccessible(true);
				field.set(instance, document.get(bindObject.getVersionColumn().getName()));
			}
			
			field = bindObject.getIdColumn().getField();
			field.setAccessible(true);
			field.set(instance, document.get(MONGO_ID));
			
			for(BindColumn bindColumn : bindObject.getColumns().values()) {
				field = bindColumn.getField();
				field.setAccessible(true);
				Object val = document.get(bindColumn.getName());
				if(val instanceof List) {
					if(bindColumn.getListTypeArgument()==null) {
						ParameterizedType pt = (ParameterizedType) field.getGenericType();
						Class<?> actualTypeArgument = (Class<?>)pt.getActualTypeArguments()[0];
						bindColumn.setListTypeArgument(actualTypeArgument);
					}
					val = JsonUtil.jsonToList(JsonUtil.objectToJson(val), bindColumn.getListTypeArgument());
					field.set(instance, val);//list document 
				}
				else {
					field.set(instance, val);
				}
				
			}
			return instance;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	
	/**
	 * 对象 转 document
	 * @param instance
	 * @return
	 */
	public static <T> Document entityToDocument(Object instance) {
		try {
			Document document = new Document();
			
			Field field = null;
			H2OContext h2oContext = CommonUtil.getTargetContext(instance.getClass());
			BindObject bindObject = h2oContext.getEntityManager().getBindObject(instance.getClass());
			if(bindObject.getVersionColumn()!=null) {
				field = bindObject.getVersionColumn().getField();
				field.setAccessible(true);
				Integer version = (Integer) field.get(instance);
				if(version==null) {
					version=1;
					field.set(instance, version);
				}
				document.put(bindObject.getVersionColumn().getName(),version);
			}
			
			field = bindObject.getIdColumn().getField();
			field.setAccessible(true);
			Object id = field.get(instance);
			if(id!=null) {
				document.put(MONGO_ID,id);
			}
			
			Object val = null;
			for(BindColumn bindColumn : bindObject.getColumns().values()) {
				field = bindColumn.getField();
				field.setAccessible(true);
				val = field.get(instance);
				if(val==null) {
					document.put(bindColumn.getName(),null);
					continue;
				}
			
				if(val instanceof List) {
					document.put(bindColumn.getName(), BsonArray.parse(JsonUtil.objectToJson(val)));
				}
				else {
					document.put(bindColumn.getName(), val);
				}
			}
			return document;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}
	

	/*//把list 和 array转换成Document[]
	private static BsonArray convertMultiple(Object object) throws Exception {
		try {
			if(object==null)return null;
			
			Object[] source = null;
			if(object instanceof List) {
				source = ((List) object).toArray();
			}
			else if(object instanceof Object[]) {
				source = (Object[]) object;
			}

			BsonArray result = new BsonArray();
			result.add(bsonValue)
			
			Document[] result = new Document[source.length];
		
			for(int i = 0 ; i<source.length ;i++) {
				result.add(convert(source[i]));
			}
			return result;
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}
	
	//把对象转换为Document
	private static Document convert(Object instance) throws Exception {
		if(instance==null)return null;
		try {
			Document document = new Document();
			document.parse(json)
			for(Field field:instance.getClass().getDeclaredFields()) {
				field.setAccessible(true);
				Object val = field.get(instance);
				if(val instanceof List || val instanceof Object[]) {
					document.put(field.getName(), convertMultiple(val));
				}
				else {
					document.put(field.getName(), val);
				}
			}
			System.out.println(document.toJson());
			return document;
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}
	*/

	
	//-------------------------------------------------------------------------------
	
	/**
	 * 
	 * @param collection
	 * @param query
	 */
	public static <T> List<T> getList(Class<T> resultClass ,MongoCollection<Document> collection, Query query) { 
	    return getList(resultClass,collection,query,null,null,null);
	}
	
	public static <T> List<T> getList(Class<T> resultClass ,MongoCollection<Document> collection, Query query,Integer pageNo ,Integer pageSize, String orderBy) {
		BasicDBObject filter = new BasicDBObject(); 
		wrapFilter(filter,query.getConditions(),pageNo,pageSize,orderBy);
		
		FindIterable<Document> iterables = collection.find(filter);
		if(!CommonUtil.isEmpty(orderBy)) {
			BasicDBObject sort = new BasicDBObject();
			String[] array = orderBy.split(SPACE);
			String fieldName = null;
			Integer order = null;
			for(String tmp:array) {
				if(order!=null && fieldName!=null) {
					break;
				}
				if(tmp.equals(SPACE))continue;
				
				if(fieldName==null&&order==null) {
					fieldName = tmp;
				}
				else {
					if(tmp.equalsIgnoreCase(DESC)) {
						order = -1;
					}
					else if(tmp.equalsIgnoreCase(ASC)) {
						order = 1;
					}
					else {
						break;
					}
				}
			}
			
		    sort.put(fieldName, order.intValue());
			iterables = iterables.sort(sort);
		}
		
		if(pageNo!=null && pageSize!=null) {
			int fromIndex = (pageNo-1)*pageSize;
			iterables = iterables.skip(fromIndex).limit(pageSize);
		}
		
		MongoCursor<Document> cursor = iterables.iterator();
		List<T> result = new ArrayList<T>();
	    while (cursor.hasNext()) {  
	    	Document tmp = cursor.next();
	    	T entity = documentToEntity(tmp,resultClass);
	    	result.add(entity); 
	    }
	    return result;
	}
	
	
	public static long getAmount(MongoCollection<Document> collection, Query query) {
		BasicDBObject filter = new BasicDBObject(); 
		wrapFilter(filter,query.getConditions(),null,null,null);
		
		return collection.count(filter);
	}
	
	private static void wrapFilter(BasicDBObject filter ,List<Condition> conditions,Integer pageNo ,Integer pageSize, String orderBy) {
		if(conditions==null)return;
		
		for(Condition condition : conditions) {
			if(ID.equals(condition.getFieldName())) {
				condition.setFieldName(MONGO_ID);
			}
			
			if(condition.getOperator()==Operator.EQ) {
				filter.put(condition.getFieldName(), condition.getVal());
			}
			else if(condition.getOperator()==Operator.LIKE) {
				Pattern pattern = Pattern.compile(LIKE_PREFIX+condition.getVal()+LIKE_SUFFIX, Pattern.CASE_INSENSITIVE);
				filter.put(condition.getFieldName(), pattern);
			}
			else if(condition.getOperator()==Operator.IN) {
				boolean isArray = condition.getVal().getClass().isArray();
				if(isArray) {
					filter.put(condition.getFieldName(), new Document(IN,condition.getVal()));
				}
				else {
					filter.put(condition.getFieldName(), new Document(IN,new Object[] {condition.getVal()}));
				}
			}
			else if(condition.getOperator()==Operator.GT) {
				filter.put(condition.getFieldName(), new Document(GT,condition.getVal()));
			}
			else if(condition.getOperator()==Operator.GTE) {
				filter.put(condition.getFieldName(), new Document(GTE,condition.getVal()));
			}
			else if(condition.getOperator()==Operator.LT) {
				filter.put(condition.getFieldName(), new Document(LT,condition.getVal()));
			}
			else if(condition.getOperator()==Operator.LTE) {
				filter.put(condition.getFieldName(), new Document(LTE,condition.getVal()));
			}
		}
	}

	
	
	private static final String LIKE_PREFIX = "^.*";
	private static final String LIKE_SUFFIX = ".*$";
	private static final String GT = "$gt";
	private static final String GTE = "$gte";
	private static final String LT = "$lt";
	private static final String LTE = "$lte";
	private static final String IN = "$in";
	private static final String SPACE = " ";
	private static final String DESC = "desc";
	private static final String ASC = "asc";
	private static final String SET = "$set";
	
	//TODO
	private static final String AND = "$and";
	private static final String BETWEEN = "between";
	private static final String OR = "or";

	

	
	
	
	
}
