package cn.regionsoft.one.core.entity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.data.dialet.core.SQLDialet;
import cn.regionsoft.one.data.persistence.Column;
import cn.regionsoft.one.data.persistence.Id;
import cn.regionsoft.one.data.persistence.Table;
import cn.regionsoft.one.data.persistence.Version;

public class BindObject {
	private Class<?> entityClass = null;
	public BindObject(Class<?> entityClass){
		this.entityClass = entityClass;
		init();
	}
	
	private void init(){
		Table tableAno = entityClass.getAnnotation(Table.class);
		if(tableAno!=null){
			tableName = tableAno.name();
		}
		if(tableName==null||tableName.trim().equals("")){
			tableName = entityClass.getSimpleName();
		}
		
		List<Field> allFields = new ArrayList<Field>();
		CommonUtil.resolveAllFields(entityClass,allFields);
		
		resolveColumns(allFields,columns);
	}
	
	private void resolveColumns(List<Field> fieldsPara,Map<String,BindColumn> columnsPara){
		BindColumn bindColumn = null;
		for(Field field:fieldsPara){
			if(idColumn==null){
				Id idAno = (Id) field.getDeclaredAnnotation(Id.class);
				if(idAno!=null){
					bindColumn = new BindColumn();
					bindColumn.setName(CommonUtil.isEmpty(idAno.name())?field.getName():idAno.name());
					bindColumn.setLength(idAno.length());
					bindColumn.setBindType(field.getType());
					bindColumn.setField(field);
					bindColumn.setIdColumn(true);
					idColumn = bindColumn;
					continue;
				}
			}
			
			Version versionAno = (Version) field.getDeclaredAnnotation(Version.class);
			if(versionColumn==null){
				if(versionAno!=null){
					bindColumn = new BindColumn();
					bindColumn.setName(versionAno.name());
					
					if(field.getType()!=Integer.class){
						throw new RuntimeException("\r\nVersion can only be Integer\r\n-"+entityClass.getName()+"."+field.getName());
					}
					bindColumn.setBindType(Integer.class);
					bindColumn.setField(field);
					versionColumn = bindColumn;
					continue;
				}
			}
			
			Column columnAno = (Column) field.getDeclaredAnnotation(Column.class);
			if(columnAno!=null){
				bindColumn = new BindColumn();
				bindColumn.setName(CommonUtil.isEmpty(columnAno.name())?field.getName():columnAno.name());
				bindColumn.setLength(columnAno.length());
				bindColumn.setBindType(field.getType());
				bindColumn.setField(field);
				
				if(!columns.containsKey(field.getName())){
					columns.put(field.getName(), bindColumn);
				}
			}
		}
	}
	
	
	
	private String tableName = null;
	/**
	 * column name - column
	 * common columns except idColumn & versionColumn
	 */
	private Map<String,BindColumn> columns = new LinkedHashMap<String,BindColumn>();
	
	private BindColumn idColumn;
	
	private BindColumn versionColumn;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Map<String, BindColumn> getColumns() {
		return columns;
	}

	public void setColumns(Map<String, BindColumn> columns) {
		this.columns = columns;
	}

	public BindColumn getIdColumn() {
		return idColumn;
	}

	public BindColumn getVersionColumn() {
		return versionColumn;
	}

	public Class<?> getEntityClass() {
		return entityClass;
	}

	public List<BindColumn> getAllColumnsExceptVersion(){
		List<BindColumn> columns = new ArrayList<BindColumn>();

		if(this.getIdColumn()!=null){
			columns.add(this.getIdColumn());
		}
		
		for(BindColumn bindColumn:this.getColumns().values()){
			columns.add(bindColumn);
		}

		return columns;
	}

	public String getDropSql(SQLDialet dialet){
		return dialet.getDropTableSql(this.getTableName());
	}
/*	
	
	
	
	public static void main(String[] args){
		SQLiteDialet sqliteDialet = new SQLiteDialet();
	
		BindObject bindObject = new BindObject(HelloEntity.class);
		System.out.println(sqliteDialet.getCreateSql(bindObject));
	}
	*/
}
