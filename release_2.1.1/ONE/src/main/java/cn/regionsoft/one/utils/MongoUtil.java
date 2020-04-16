package cn.regionsoft.one.utils;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import org.bson.Document;
import cn.regionsoft.one.core.entity.BindColumn;
import cn.regionsoft.one.core.entity.BindObject;

public class MongoUtil {
	public static final String ID = "_id";
	
	public static <T> T documentToObject(Document document, BindObject bindObject) throws InstantiationException, IllegalAccessException {
		if(document==null)return null;
		
		@SuppressWarnings("unchecked")
		T result = (T) bindObject.getEntityClass().newInstance();
		Collection<BindColumn> columns = bindObject.getColumns().values();
		Field field = null;
		//Object value = null;
		for(BindColumn bindColumn:columns){
			field = bindColumn.getField();
			setField(result,field,document.get(field.getName()));	
		}
		
		BindColumn idColumn = bindObject.getIdColumn();
		if(idColumn!=null){
			field = idColumn.getField();
			setField(result,field,document.get(ID));	
		}
		
		BindColumn versionColumn = bindObject.getVersionColumn();
		if(versionColumn!=null){
			field = versionColumn.getField();
			setField(result,field,document.get(field.getName()));
		}
		return result;
	}
	
	private static void setField(Object result,Field field,Object val) throws IllegalArgumentException, IllegalAccessException {
		field.setAccessible(true);
		if(val==null) {
			field.set(result, val);
			return;
		}
		
		if(field.getType()!=val.getClass()) {
			if(val.getClass()==Long.class) {
				if(field.getType()==Integer.class) {
					field.set(result, ((Long) val).intValue());
					return;
				}
			}
		}
		else {
			field.set(result, val);
			return;
		}
		throw new RuntimeException("val type not matched");
	}
	
	
	public static <T> Document objectToDocument(T instance,BindObject bindObject) throws IllegalArgumentException, IllegalAccessException {
		Document document = new Document();
		
		Collection<BindColumn> columns = bindObject.getColumns().values();
		
		Field field = null;
		Object value = null;
		for(BindColumn bindColumn:columns){
			field = bindColumn.getField();
			field.setAccessible(true);
			value = field.get(instance);
	
			/*if(bindColumn.getBindType() == java.util.Date.class){
				if(value==null){
					document.append(bindColumn.getName(), null);
				}
				else{
					java.util.Date utilDate = (java.util.Date) value;
					java.sql.Timestamp sqlDate = new java.sql.Timestamp(utilDate.getTime());
					document.append(bindColumn.getName(), sqlDate);
				}
			}
			else if(bindColumn.getBindType() == Boolean.class){
				document.append(bindColumn.getName(), (Boolean) value);
			}
			else{
				document.append(bindColumn.getName(), value);
			}*/
			
			document.append(bindColumn.getName(), value);
		}
		
		BindColumn idColumn = bindObject.getIdColumn();
		if(idColumn!=null){
			field = idColumn.getField();
			field.setAccessible(true);
			value = field.get(instance);
			document.append(ID, value);
		}
		
		BindColumn versionColumn = bindObject.getVersionColumn();
		if(versionColumn!=null){
			field = versionColumn.getField();
			field.setAccessible(true);
			Integer versionNo = (Integer) field.get(instance);
			
			if(versionNo==null) versionNo = 0;
			else versionNo = versionNo+1;
			
			field.set(instance, versionNo);
			document.append(versionColumn.getName(), value);
		}
		return document;
	}
}
