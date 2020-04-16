package cn.regionsoft.one.core;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import cn.regionsoft.one.bigdata.core.persist.RDEntity;
import cn.regionsoft.one.core.entity.BindColumn;
import cn.regionsoft.one.core.entity.BindObject;

public class RDEntityUtil {
	public static Object transferToEntity(RDEntity rdEntity,BindObject bindObject,Class<?> entityClass) throws Exception {
		if(rdEntity==null)return null;
		Iterator<Entry<String,Object>> iterator = rdEntity.getProperties().entrySet().iterator();
		Object tmpObject = entityClass.newInstance();
		Entry<String,Object> tmpEntry = null;
		Field field = null;
		BindColumn bindColumn = null;
		Object tmpValue = null;
		Map<String,BindColumn> columns = bindObject.getColumns();
		while(iterator.hasNext()) {
			tmpEntry = iterator.next();
			bindColumn = columns.get(tmpEntry.getKey());
			if(bindColumn==null)continue;
			field = bindColumn.getField();
			tmpValue = tmpEntry.getValue();
			
//			if(bindColumn.getBindType() == java.util.Date.class){
//				field.set(tmpObject, tmpValue);
//			}
			field.set(tmpObject, tmpValue);
		}
		
		
		field = bindObject.getIdColumn().getField();
		if(bindObject.getIdColumn().getBindType() == Long.class){
			field.set(tmpObject, Long.valueOf(rdEntity.getId()));
		}
		else {
			field.set(tmpObject, rdEntity.getId());
		}
		
	
		return tmpObject;
	}
}
