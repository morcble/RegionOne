package cn.regionsoft.one.core.contextinfo.dto;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import cn.regionsoft.one.annotation.InstanceAnoType;
import cn.regionsoft.one.annotation.NoInstanceAnoType;
import cn.regionsoft.one.annotation.tag.RequestMapping;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.AnnotatedClassHub;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.EntityManager;
import cn.regionsoft.one.core.H2OContext;
import cn.regionsoft.one.core.SystemContext;
import cn.regionsoft.one.core.contextinfo.ManagedBeanDetailPojo;
import cn.regionsoft.one.core.contextinfo.MethodInfoPojo;
import cn.regionsoft.one.core.dbconnection.SQLConnection;
import cn.regionsoft.one.core.dbconnection.SQLConnectionManager;
import cn.regionsoft.one.core.entity.BindColumn;
import cn.regionsoft.one.core.entity.BindObject;
import cn.regionsoft.one.core.entity.SQLEntityManager;
import cn.regionsoft.one.core.ids.IDGenerator;
import cn.regionsoft.one.data.dialet.core.SQLDialet;
import cn.regionsoft.one.data.persistence.H2OEntity;

public class H2OContextDetail{
	private static final Logger logger = Logger.getLogger(H2OContextDetail.class);
	
	private String name;
	private boolean persist;
	private List<ManagedBeanDetailPojo> managedBeanDetailPojoLs = new ArrayList<ManagedBeanDetailPojo>();
	private SystemContext systemContext = SystemContext.getInstance();
	/**
	 * @return information of all beans managed by context  
	 */
	public List<ManagedBeanDetailPojo> getManagedBeanDetailPojoLs() {
		return managedBeanDetailPojoLs;
	}

	private List toPersistBeanData = new ArrayList();
	private List toPersistMethodData = new ArrayList();
	
	public H2OContextDetail(H2OContext h2oContext, boolean persist,String name) throws SQLException {
		this.persist = persist;
		this.name = name;
		scanInfo(h2oContext);
	}

	private void scanInfo(H2OContext h2oContext) throws SQLException {
		managedBeanDetailPojoLs.clear();
		toPersistBeanData.clear();
		toPersistMethodData.clear();
		
		AnnotatedClassHub annotatedClassHub = h2oContext.getAnnotatedClassHub();
		HashSet<Class<?>> entities = annotatedClassHub.getClassSetByAnnotation(NoInstanceAnoType.Entity);
		Class managedBeanDetail = null;
		Class methodInfo = null;
		for(Class<?> entity:entities){
			if(managedBeanDetail!=null&&methodInfo!=null)break;
			if("ManagedBeanDetail".equals(entity.getSimpleName())){
				managedBeanDetail = entity;
			}
			else if("MethodInfo".equals(entity.getSimpleName())){
				methodInfo = entity;
			}
		}
		
		if(managedBeanDetail==null||methodInfo==null) return;
		//-----------------------------------------------------------------------------

		
		resolveBeans("Entity",entities,managedBeanDetail,methodInfo,toPersistBeanData,toPersistMethodData);
		resolveBeans("Controller",annotatedClassHub.getClassSetByAnnotation(InstanceAnoType.Controller),managedBeanDetail,methodInfo,toPersistBeanData,toPersistMethodData);
		resolveBeans("Component",annotatedClassHub.getClassSetByAnnotation(InstanceAnoType.Component),managedBeanDetail,methodInfo,toPersistBeanData,toPersistMethodData);
		resolveBeans("Resource",annotatedClassHub.getClassSetByAnnotation(InstanceAnoType.Resource),managedBeanDetail,methodInfo,toPersistBeanData,toPersistMethodData);
		resolveBeans("Service",annotatedClassHub.getClassSetByAnnotation(InstanceAnoType.Service),managedBeanDetail,methodInfo,toPersistBeanData,toPersistMethodData);
		resolveBeans("Dao",annotatedClassHub.getClassSetByAnnotation(InstanceAnoType.Dao),managedBeanDetail,methodInfo,toPersistBeanData,toPersistMethodData);
		
		if(persist){
			persistData(h2oContext);
		}
	}
	
	private void persistData(H2OContext h2oContext) throws SQLException{
		PreparedStatement truncatePst = null;
		PreparedStatement pst = null;
		SQLConnection connection = null;
		EntityManager entityManager = h2oContext.getEntityManager();
		try{
			try{
				connection = SQLEntityManager.getConnection(h2oContext);
				connection.setAutoCommit(false);
				BindObject  bindObject = null;
				List<BindColumn> columns = null;
				for(Object instance:toPersistBeanData){
					if(pst==null){
						bindObject = entityManager.getBindObject(instance.getClass());
						SQLDialet dialet = (SQLDialet) h2oContext.getEntityManager().getDialet();
						
						String tableName = bindObject.getTableName();
						String truncateSql = dialet.getTruncateTableSql(tableName);
						truncatePst = connection.prepareStatement(truncateSql);
						truncatePst.executeUpdate();
		
						String preparedSql = dialet.getInsertSql(bindObject);
						pst = connection.prepareStatement(preparedSql);
						columns = bindObject.getAllColumnsExceptVersion();
					}
					
					setPrepareStm(pst, columns,instance,bindObject.getVersionColumn());
					pst.addBatch();
				}
				pst.executeBatch();
				connection.commit();
			}
			catch(Exception e){
				throw e;
			}
			finally{
				CommonUtil.closeQuietly(pst);
				CommonUtil.closeQuietly(truncatePst);
				pst = null;
				try {
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				SQLConnectionManager.releaseConnection(h2oContext);
			}
			
			
			try{
				connection = SQLEntityManager.getConnection(h2oContext);
				connection.setAutoCommit(false);
				BindObject  bindObject = null;
				List<BindColumn> columns = null;
				for(Object instance:toPersistMethodData){
					if(pst==null){
						bindObject = entityManager.getBindObject(instance.getClass());
						SQLDialet dialet = (SQLDialet) h2oContext.getEntityManager().getDialet();
						
						String tableName = bindObject.getTableName();
						String truncateSql = dialet.getTruncateTableSql(tableName);
						truncatePst = connection.prepareStatement(truncateSql);
						truncatePst.executeUpdate();
						
						String preparedSql = dialet.getInsertSql(bindObject);
						pst = connection.prepareStatement(preparedSql);
						columns = bindObject.getAllColumnsExceptVersion();
					}
					setPrepareStm(pst, columns,instance,bindObject.getVersionColumn());
					pst.addBatch();
				}
				pst.executeBatch();
				connection.commit();
			}
			catch(Exception e){
				throw e;
			}
		}
		catch(Exception e){
			logger.error(e);
			try {
				connection.rollback();
			} catch (SQLException e1) {
				throw e1;
			}
		}
		finally{
			try {
				connection.setAutoCommit(true);
			} catch (SQLException e) {
				logger.error(e);
			}
			CommonUtil.closeQuietly(pst);
			CommonUtil.closeQuietly(truncatePst);
			SQLConnectionManager.releaseConnection(h2oContext);
			pst = null;
		}
	}
	
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
	
	private void resolveBeans(String beanType, HashSet<Class<?>> beanSet,Class managedBeanDetail,Class methodInfo, List toPersistBeanData, List toPersistMethodData){
		List<ClassWrapper> ls = new ArrayList<ClassWrapper>();
		for(Class<?> beanClass:beanSet){
			ClassWrapper classWrapper = new ClassWrapper(beanClass);
			ls.add(classWrapper);
		}
		Collections.sort(ls);
		
		Date now = new Date();
		for(ClassWrapper classWrapper:ls){
			Class<?> beanClass = classWrapper.getBeanClass();
			try {
				if("Controller".equals(beanType)&&classWrapper.getBeanClass().getSimpleName().equals("CombinedRequestController")){
					continue;
				}
		
				ManagedBeanDetailPojo managedBeanDetailPojo = new ManagedBeanDetailPojo();
				managedBeanDetailPojoLs.add(managedBeanDetailPojo);
				
				managedBeanDetailPojo.setSystemId("LOCAL");
				managedBeanDetailPojo.setId(IDGenerator.getStringID());
				managedBeanDetailPojo.setContextName(this.name);
				
				String beanName = beanClass.getName();
				managedBeanDetailPojo.setName(beanClass.getSimpleName());
				managedBeanDetailPojo.setPackageName(beanName.substring(0,beanName.lastIndexOf(".")));
				managedBeanDetailPojo.setBeanType(beanType);
				managedBeanDetailPojo.setSvcType("Local");
				managedBeanDetailPojo.setCreateBy("System");
				managedBeanDetailPojo.setUpdateBy("System");
				managedBeanDetailPojo.setCreateDt(now);
				managedBeanDetailPojo.setUpdateDt(now);
				String baseUrl = "";
				if("Controller".equals(beanType)){
					RequestMapping requestMapping = beanClass.getAnnotation(RequestMapping.class);
					if(requestMapping!=null){
						baseUrl = requestMapping.value();
					}
				}
				
				H2OEntity bean = (H2OEntity) managedBeanDetail.newInstance();
				CommonUtil.copyProperties(managedBeanDetailPojo,bean);
				toPersistBeanData.add(bean);
				
				for(Method method:beanClass.getDeclaredMethods()){
					MethodInfoPojo methodInfoPojo = new MethodInfoPojo();
					managedBeanDetailPojo.addMethodInfoPojo(methodInfoPojo);
					methodInfoPojo.setId(IDGenerator.getStringID());
					methodInfoPojo.setDetailId(Long.valueOf(managedBeanDetailPojo.getId()));
					methodInfoPojo.setName(method.getName());
					if("Controller".equals(beanType)){
						RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
						if(requestMapping!=null){
							methodInfoPojo.setUrl(baseUrl+requestMapping.value());
						}
					}
					StringBuilder sb = new StringBuilder("");
					Class[] parameterTypes= method.getParameterTypes();
					if(parameterTypes!=null){
						for(int i = 0 ; i <parameterTypes.length ; i ++){
							sb.append(parameterTypes[i].getName());
							sb.append(" arg");
							sb.append(i);
							if(i!=parameterTypes.length-1){
								sb.append(" , ");
							}
						}
					}
					
					methodInfoPojo.setInputType(sb.toString());
					methodInfoPojo.setReturnType(method.getReturnType().getName());
					methodInfoPojo.setCreateBy("System");
					methodInfoPojo.setUpdateBy("System");
					methodInfoPojo.setCreateDt(now);
					methodInfoPojo.setUpdateDt(now);
					
					H2OEntity methodInfoBean = (H2OEntity) methodInfo.newInstance();
					CommonUtil.copyProperties(methodInfoPojo,methodInfoBean);
					toPersistMethodData.add(methodInfoBean);
				}
			} catch (Exception e) {
				logger.error(e);
			} 
			//entity.getDeclaredMethods()
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
