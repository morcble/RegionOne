package cn.regionsoft.one.bigdata.enums;

import java.util.HashMap;
import java.util.Map;

public enum DataType {
	INT,
	LONG,
	FLOAT,
	DOUBLE,
	BOOLEAN,
	STRING, 
	DATE,//存DB 为long
	BIGDECIMAL;
	
	private static Map<String,DataType> map = new HashMap<String,DataType>();
	static {
		for(DataType dataType :DataType.values()) {
			map.put(dataType.name(), dataType);
		}
	}
	
	public static DataType getDataType(String str) {
		return map.get(str);
	}
}
