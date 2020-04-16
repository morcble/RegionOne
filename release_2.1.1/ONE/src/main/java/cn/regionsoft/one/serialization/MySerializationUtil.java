package cn.regionsoft.one.serialization;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import cn.regionsoft.one.rpc.common.RpcRequest;

import javassist.CannotCompileException;
import javassist.NotFoundException;




public class MySerializationUtil {
	private static Map<Class,Schema> schemaMap = new ConcurrentHashMap<Class,Schema>();
	
	public static  byte[] serialize(Object obj) throws Exception {
		Class classType = obj.getClass();
		Schema schema = schemaMap.get(classType);
		if(schema==null) {
			schema = RuntimeSchema.getSchema(classType);
			schemaMap.put(classType, schema);
		}
		return StreamUtil.toByteArray(obj, schema);
	}
	public static <T> T deserialize(byte[] data, Class<T> cls) {
		return null;
	}
	
	//810
	public static void main(String[] args) throws Exception {
    	RpcRequest a = new RpcRequest();
    	a.setRegisterPath("sadfasdf");
    	a.setMethodName("sdfasdfsdf");
    	//a.setParameters(new Object[] {"asdasd"});
    
    	MySerializationUtil.deserialize(MySerializationUtil.serialize(a),RpcRequest.class);
    	
    	long time =System.currentTimeMillis();
    	for(int i = 0 ; i <600000;i++) {
    		//for(int i = 0 ; i <1;i++) {
    		MySerializationUtil.deserialize(MySerializationUtil.serialize(a),RpcRequest.class);
    	}
    	System.out.println(System.currentTimeMillis()-time);
	}

}
