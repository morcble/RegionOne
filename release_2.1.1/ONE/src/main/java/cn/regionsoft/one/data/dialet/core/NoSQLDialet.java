package cn.regionsoft.one.data.dialet.core;

import java.util.HashMap;
import java.util.Map;

import cn.regionsoft.one.bigdata.enums.DataType;

public abstract class NoSQLDialet implements Dialet{
	protected Map<Class<?>,DataType> typeMapping = new HashMap<Class<?>,DataType>();
	
	public abstract void config();
	
	protected void init() {
		config();
	}
}
