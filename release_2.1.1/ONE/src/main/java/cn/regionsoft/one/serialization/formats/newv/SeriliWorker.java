package cn.regionsoft.one.serialization.formats.newv;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import cn.regionsoft.one.serialization.formats.core.SeriziDataType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;


public class SeriliWorker {
	private Map<Class,Field[]> fieldsMap = new HashMap<Class,Field[]>();
	private ByteBuf threadBindBuf = ByteBufAllocator.DEFAULT.buffer(10);
	
	/*
	 * byte[] 转为对象
	 * @param obj
	 * @param classType
	 * @return
	 * @throws Exception //[12, 0, 0, 0, 0, 1, 0, 0, 0, 1, 97, 1, 0, 0, 0, 1, 97]
	 */
	public Object deserialize(ByteBuf byteBuf,Class<?> targetClass) throws Exception {
		int dataType = byteBuf.readByte();
		switch(dataType) {
			case 0:
				return deserializeObject(byteBuf,targetClass);
			case 1: 
				return StringFormat.getValFromSerialized(byteBuf, 4,byteBuf.readIntLE());
		}
		return null;
	}
	
	
	//[12, 0, 0, 0, 0, 1, 0, 0, 0, 1, 97, 1, 0, 0, 0, 1, 97]
	private Object deserializeObject(ByteBuf byteBuf,Class<?> targetClass) throws Exception {
		Object instance = targetClass.newInstance();
		//class instance	
		Field[] fields = fieldsMap.get(targetClass);
		if(fields==null) {
			fields = targetClass.getDeclaredFields();
			for(Field tmp:fields) {
				tmp.setAccessible(true);
			}
			fieldsMap.put(targetClass, fields);
		}
		
		for(Field tmp:fields) {
			tmp.set(instance, deserializeFieldObject(byteBuf,tmp.getType()));
		}
		return instance;
	}
	
	
	private Object deserializeFieldObject(ByteBuf byteBuf,Class<?> targetClass) throws Exception {
		int dataType = byteBuf.readByte();//第1个字节是类型
			
		switch(dataType) {
			case 0:
				//byteBuf.skipBytes(5);
				return deserializeObject(byteBuf,targetClass);
			case 1: 
				return StringFormat.getValFromSerialized(byteBuf,0,byteBuf.readIntLE());
			case 10://byte[]
				int length = byteBuf.readIntLE();
				if(length ==0)return null;
				else {
					byte[] tmp = new byte[length];
					byteBuf.readBytes(tmp);
					return tmp;
				}
		}
		return null;
	}
	

	
	/**对象转换为byte[]
	 * serialize with bytebuf
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	public ByteBuf serialize(Object obj) throws Exception {
		threadBindBuf.clear();
		return serialize(obj,threadBindBuf);
	}
	
	public ByteBuf serialize(Object obj,ByteBuf byteBuf) throws Exception {
		Class targetClass = obj.getClass();
		if(targetClass==String.class) {
			return StringFormat.getBytes((String) obj,byteBuf);
		}
		/*else if(targetClass==Integer.class) {
			return IntegerFormat.getSerializedBytes((Integer) obj);
		}
		else if(targetClass==Long.class) {
			return LongFormat.getSerializedBytes((Long) obj);
		}
		else if(targetClass==Float.class) {
			return FloatFormat.getSerializedBytes((Float) obj);
		}
		else if(targetClass==Double.class) {
			return DoubleFormat.getSerializedBytes((Double) obj);
		}*/
		else if(targetClass.isArray()) {
			//TODO
		}
		else if(obj instanceof List) {
			//TODO
		}
		else if(obj instanceof Map) {
			//TODO
		}
		
		//class instance	
		Field[] fields = fieldsMap.get(targetClass);
		if(fields==null) {
			fields = obj.getClass().getDeclaredFields();
			for(Field tmp:fields) {
				tmp.setAccessible(true);
			}
			fieldsMap.put(targetClass, fields);
		}
		
		byteBuf.writeByte((byte) SeriziDataType.Class.ordinal());
		for(Field tmp:fields) {
			Object val = tmp.get(obj);
			wrapFieldBytes(val,tmp.getType(),byteBuf);
		}

		return byteBuf;
	}
	
	
	
	
	public void wrapFieldBytes(Object obj,Class targetClass,ByteBuf byteBuf) throws Exception {
		if(targetClass == Object.class) {
			targetClass = obj.getClass();
		}
		if(targetClass.isArray()) {
			if("[B".equals(targetClass.getName())){//byte[]
				if(obj!=null) {
					byte[] tmp = (byte[]) obj;
					byteBuf.writeByte(SeriziDataType.byteT.ordinal());
					byteBuf.writeIntLE(tmp.length);
					byteBuf.writeBytes(tmp);
				}
				else {
					byteBuf.writeByte(SeriziDataType.byteT.ordinal());
					byteBuf.writeIntLE(0);
				}
				
			}
			return;
		}
		else if(targetClass==String.class) {
			StringFormat.wrapBytes((String) obj,byteBuf);
			return;
		}
		/*else if(targetClass==Integer.class) {
			IntegerFormat.getSerializedBytes((Integer) obj);
			return;
		}
		else if(targetClass==Long.class) {
			 LongFormat.getSerializedBytes((Long) obj);
			return;
		}
		else if(targetClass==Float.class) {
			FloatFormat.getSerializedBytes((Float) obj);
			return;
		}
		else if(targetClass==Double.class) {
			DoubleFormat.getSerializedBytes((Double) obj);
			return;
		}
		else if(targetClass.isArray()) {
			//TODO
		}
		else if(obj instanceof List) {
			//TODO
		}
		else if(obj instanceof Map) {
			//TODO
		}*/
		
		//class instance
		wrapObject(obj,targetClass,byteBuf);
	}
	
	private void wrapObject(Object obj,Class targetClass,ByteBuf byteBuf) throws Exception {
		//class instance	
		Field[] fields = fieldsMap.get(targetClass);
		if(fields==null) {
			fields = obj.getClass().getDeclaredFields();
			for(Field tmp:fields) {
				tmp.setAccessible(true);
			}
			fieldsMap.put(targetClass, fields);
		}
		
		byteBuf.writeByte((byte) SeriziDataType.Class.ordinal());
		for(Field tmp:fields) {
			Object val = tmp.get(obj);
			wrapFieldBytes(val,tmp.getType(),byteBuf);
		}

	}
	
//----------------------------------------------------------------------------------------------
	
}
