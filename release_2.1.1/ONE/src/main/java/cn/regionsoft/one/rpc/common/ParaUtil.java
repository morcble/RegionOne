package cn.regionsoft.one.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class ParaUtil {
	private static final String DELIMETER = ";";
	public static String paseParaTypesToString(Class<?>[] parameterTypes) {
		StringBuilder sb = new StringBuilder();
		for(Class tmp:parameterTypes) {
			sb.append(tmp.getName());
			sb.append(DELIMETER);
		}
		return sb.toString();
	}
	
	public static Class<?>[]  paseStringToParaTypes(String paraTypeStr) {
		try {
			if(paraTypeStr!=null) {
				String[] typeArray = paraTypeStr.split("\\;");
				Class<?>[] result = new Class<?>[typeArray.length];
				for(int i = 0 ; i <typeArray.length;i++) {
					result[i] = Class.forName(typeArray[i]);
				}
				return result;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return new Class<?>[0];
	}
	
	//TODO 嵌套  把对象转换为byte
	public static byte[] getBytes(Object object, Class<?> class1) {
		return SerializationUtil.serialize(object);
	}
	
	//TODO 嵌套  把bytes 还原为对象
	public static Object[] getObjectArrayFormBytes(byte[] bytes,Class<?>[] classTypes) {
		int length = classTypes.length;
		Object[] result = new Object[length];
		ByteBuf tmpBuf = cache.get();
		if(tmpBuf==null) {
			tmpBuf = ByteBufAllocator.DEFAULT.buffer(10);
			cache.set(tmpBuf);
		}
		tmpBuf.writeBytes(bytes);
		int paraByteLength = 0;
		byte[] tmpBytes = null;
		for(int i = 0 ; i <length;i++) {
			paraByteLength = tmpBuf.readIntLE();
			tmpBytes = new byte[paraByteLength];
			tmpBuf.readBytes(tmpBytes);
			result[i] = SerializationUtil.deserialize(tmpBytes, classTypes[i]);
		}
		tmpBuf.clear();
		return result;
	}
	
	static ThreadLocal<ByteBuf> cache = new ThreadLocal<ByteBuf>();
	public static byte[] getBytes(Object[] parameters, Class<?>[] parameterTypes) {
		ByteBuf tmpBuf = cache.get();
		if(tmpBuf==null) {
			tmpBuf = ByteBufAllocator.DEFAULT.buffer(10);
			cache.set(tmpBuf);
		}
		
		int paraCount = parameters.length;
		
		byte[] tmpBytes = null;
		int currentIntIndex = 0;
		for(int i = 0 ; i <paraCount ; i++) {
			tmpBytes = ParaUtil.getBytes(parameters[i],parameterTypes[i]);
			tmpBuf.writeIntLE(currentIntIndex);
			tmpBuf.writeBytes(tmpBytes);
			tmpBuf.setIntLE(currentIntIndex, tmpBytes.length);
			currentIntIndex+=tmpBytes.length+1;
		}
		
		tmpBytes = new byte[tmpBuf.readableBytes()];
		tmpBuf.readBytes(tmpBytes);
		tmpBuf.clear();
		/*int paraCount = parameters.length;
    	byte[][] objBytes = new byte[paraCount][];
    	int totalLength = 0;
        for(int i = 0 ; i <paraCount ; i++) {
        	objBytes[i] =  ParaUtil.getBytes(parameters[i],parameterTypes[i]);
        	totalLength+=objBytes[i].length;
        }
        
        paraBytes = new byte[totalLength];
        int destPos = 0;
        for(int i = 0 ; i <paraCount ; i++) {
        	System.arraycopy(objBytes[i], 0, paraBytes, destPos, objBytes[i].length);
        	destPos+=objBytes[i].length;
        }*/
		return tmpBytes;
	}

}
