package cn.regionsoft.one.serialization;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
//import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;

public class StreamUtil {
	private static Map<Class,Field[]> fieldsMap = new ConcurrentHashMap<Class,Field[]>();

	public static byte[] toByteArray(Object obj, Schema schema) throws Exception {
		Class tmpClass = obj.getClass();
		byte[] result = null;
		int length = 0 ;
		
		if(tmpClass==String.class) {//字符串
			String str = (String) obj;
		}
		else if(tmpClass==Integer.class) {//数字
			//TODO
		}
		else if(tmpClass==Long.class) {//数字
			//TODO
		}
		else if(tmpClass==Float.class) {//数字
			//TODO
		}
		else if(tmpClass==Double.class) {//数字
			//TODO
		}
		else if(tmpClass.isArray()) {
			Object[] array = (Object[]) obj;
			
		}
		else if(tmpClass.isInstance(List.class)) {
			List tmp = (List) obj;
		}
		else if(tmpClass.isInstance(Map.class)) {
			Map tmp = (Map) obj;
		}
		else if(tmpClass.isPrimitive()) {//基本类型
			throw new Exception("Primitive type is not supported :"+obj);
		}
		else {
			Field[] fields = fieldsMap.get(tmpClass);
			if(fields==null) {
				fields = obj.getClass().getDeclaredFields();
				fieldsMap.put(tmpClass, fields);
			}
			
			for(Field tmp:fields) {
				tmp.setAccessible(true);
				tmp.get(obj);
			}
		}
		
		
		return null;
	}
	
	public static void main(String[] args) {
		System.out.println(Byte.MAX_VALUE);
	}

}
