package cn.regionsoft.one.data.dialet.core;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.core.entity.BindColumn;
import cn.regionsoft.one.core.entity.BindObject;


public abstract class SQLDialet implements Dialet{
	protected Map<Class<?>,Integer> typeMapping = new HashMap<Class<?>,Integer>();  
	
	public abstract void config();
	
	protected void init() {
		config();
	}
	
	public abstract String getTableExsitsQuery(String tableName,String schema);
	
	public String getCreateTableSql(BindObject bindObject){
		StringBuilder sb = new StringBuilder();
		sb.append("create table ");
		sb.append(bindObject.getTableName());
		sb.append(" (");
		sb.append(CommonUtil.getColumnsSql(bindObject,this));
		sb.append(")");
		return sb.toString();
	}
	
	
	/**
	 * C
	 * @param bindObject
	 * @return
	 */
	public String getInsertSql(BindObject bindObject) {
		StringBuilder sb = new StringBuilder();
		sb.append("insert into ");
		sb.append(bindObject.getTableName());
		sb.append(" (");
		
		StringBuilder appendClause = new StringBuilder();
		if(bindObject.getIdColumn()!=null){
			sb.append(bindObject.getIdColumn().getName());
			sb.append(",");
			
			appendClause.append("?,");
		}
		for(BindColumn bindColumn:bindObject.getColumns().values()){
			sb.append(bindColumn.getName());
			sb.append(",");
			
			appendClause.append("?,");
		}
		if(bindObject.getVersionColumn()!=null){
			sb.append(bindObject.getVersionColumn().getName());
			sb.append(",");
			
			appendClause.append("?,");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append(") values(");
		
		appendClause.deleteCharAt(appendClause.length()-1);
		sb.append(appendClause.toString());
		
		sb.append(")");
		return sb.toString();
	}
	
	/**
	 * R
	 * @param bindObject
	 * @return
	 */
	public String getFindSql(BindObject bindObject) {
		StringBuilder sb = new StringBuilder();
		sb.append("select * from ");
		sb.append(bindObject.getTableName());
		sb.append(" where ");
		sb.append(bindObject.getIdColumn().getName());
		sb.append(" = ?");
		return sb.toString();
	}
	
	
	/**
	 * RA
	 * @param bindObject
	 * @return
	 */
	public String getFindAllSql(BindObject bindObject) {
		StringBuilder sb = new StringBuilder();
		sb.append("select * from ");
		sb.append(bindObject.getTableName());
		sb.append(" where softDelete = 0 order by id desc");
		return sb.toString();
	}

	/**
	 * D
	 * @param bindObject
	 * @return
	 */
	public String getDeleteSql(BindObject bindObject) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ");
		sb.append(bindObject.getTableName());
		sb.append(" where ");
		sb.append(bindObject.getIdColumn().getName());
		sb.append(" = ?");
		return sb.toString();
	}
	
	/**
	 * U
	 * @param bindObject
	 * @param updateColumns 
	 * @return
	 */
	public String getUpdateSql(BindObject bindObject, Map<String,BindColumn> dueToUpDateMap) {
		StringBuilder sb = new StringBuilder();
		sb.append("update ");
		sb.append(bindObject.getTableName());
		sb.append(" set ");

		
		for(BindColumn bindColumn:bindObject.getColumns().values()){
			if(!dueToUpDateMap.containsKey(bindColumn.getName())) continue;
			
			sb.append(bindColumn.getName());
			sb.append(" = ?,");
		}
		if(bindObject.getVersionColumn()!=null){
			sb.append(bindObject.getVersionColumn().getName());
			sb.append(" = ?,");
		}
		sb.deleteCharAt(sb.length()-1);
		
		
		sb.append(" where ");
		sb.append(bindObject.getIdColumn().getName());
		sb.append(" = ?");
		return sb.toString();
	}
	
	public String getDropTableSql(String tableName) {
		return MessageFormat.format("drop table {0}", tableName);
	}
	
	/*
	 * 
	 */
	protected static Map<Integer,String> typeMapString = new HashMap<Integer,String>();  
	
	
	private static String getSqlStrByType(int sqlType){
		return typeMapString.get(sqlType);
	}
	
	public static String getSqlStrByFieldType(SQLDialet dialet,Class<?> fieldType){
		return getSqlStrByType(dialet.getTypeMapping().get(fieldType));
	}

	public Map<Class<?>, Integer> getTypeMapping() {
		return typeMapping;
	}
	

	public abstract String getPagenationQuery(String sql, Integer pageNo, Integer pageSize);

	public abstract String getTruncateTableSql(String tableName);

	public abstract String getSqlStrForTextField();
}
