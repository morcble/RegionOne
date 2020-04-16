package cn.regionsoft.one.serialization;

import java.lang.reflect.Field;

public class RuntimeSchema {

	public static Schema getSchema(Class<? extends Object> class1) {
		Schema schema = new Schema();
		
		Field[] fields = class1.getFields();
		
		return schema;
	}

}
