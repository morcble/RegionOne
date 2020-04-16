package cn.regionsoft.one.bigdata.core.persist;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import cn.regionsoft.one.core.RDEntityUtil;
import cn.regionsoft.one.core.entity.BindColumn;
import cn.regionsoft.one.core.entity.BindObject;
import cn.regionsoft.one.data.persistence.H2OEntity;

public class RDListResultWrapper<T> {
	private List<T> list;
	private String nextStartRowKey;
	public List<T> getList() {
		return list;
	}
	public void setList(List<T> list) {
		this.list = list;
	}
	public String getNextStartRowKey() {
		return nextStartRowKey;
	}
	public void setNextStartRowKey(String nextStartRowKey) {
		this.nextStartRowKey = nextStartRowKey;
	}
	
	public static <T> RDListResultWrapper<T> toWrapper(RDEntityListResult rdEntityListResult,BindObject bindObject,Class<?> entityClass) throws Exception {
		if(rdEntityListResult==null)return null;
		
		RDListResultWrapper<T> rdListResultWrapper = new RDListResultWrapper<T>();
		
		
		List<T> list = new ArrayList<T>();
		for(RDEntity rdEntity : rdEntityListResult.getList()) {
			list.add((T) RDEntityUtil.transferToEntity(rdEntity, bindObject, entityClass));
		}
		
		rdListResultWrapper.setList(list);
		rdListResultWrapper.setNextStartRowKey(rdEntityListResult.getNextStartRowKey());
		
		return rdListResultWrapper;
	}
	

}
