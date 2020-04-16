package cn.regionsoft.one.serialization.formats.newv;

import cn.regionsoft.one.serialization.formats.core.SeriziDataType;

public class LongFormat{
	public static final byte DATATYPE = (byte) SeriziDataType.Long.ordinal();
	
	public static byte[] getSerializedBytes(Long para) {
		if(para==null)return null;
		
		byte[] content = MathUtil.longToBytes(para);
		
		int contentLength = content.length;
		byte[] lengthByte = MathUtil.intToBytes(contentLength);
		byte length = (byte) lengthByte.length;
		int totalLength = 1+lengthByte.length+contentLength;
		byte[] result = new byte[totalLength+1];
		result[0] = DATATYPE;
		result[1] = length;
		
		System.arraycopy(lengthByte, 0, result, 2, length);
		System.arraycopy(content, 0, result, 2+length, contentLength);
		return result;
	}
	
	public static Long getValFromSerialized(byte[] bytes) {
		int lengthByte = bytes[1];
		//get content length
		int contentLength = MathUtil.bytesToInt(bytes, 2,lengthByte+2);
		
		byte[] strBytes = new byte[contentLength];
		System.arraycopy(bytes, lengthByte+2, strBytes, 0, contentLength);
		return MathUtil.bytesToLong(strBytes, 0,strBytes.length);
	}
	
	
	
	public static byte[] getBytes(Long para) {
		if(para==null)return null;
		
		byte[] content = MathUtil.longToBytes(para);
		
		int contentLength = content.length;
		byte[] lengthByte = MathUtil.intToBytes(contentLength);
		byte length = (byte) lengthByte.length;
		int totalLength = 1+lengthByte.length+contentLength;
		byte[] result = new byte[totalLength];
		result[0] = length;
		
		System.arraycopy(lengthByte, 0, result, 1, length);
		System.arraycopy(content, 0, result, 1+length, contentLength);
		return result;
	}
	
	public static Long getVal(byte[] bytes) {
		int lengthByte = bytes[0];
		//get content length
		int contentLength = MathUtil.bytesToInt(bytes, 1,lengthByte+1);
		
		byte[] strBytes = new byte[contentLength];
		System.arraycopy(bytes, lengthByte+1, strBytes, 0, contentLength);
		return MathUtil.bytesToLong(strBytes, 0,strBytes.length);
	}
	
	
	public static void main(String[] args) throws Exception {
		long time = System.currentTimeMillis();
		//String x = "fasdfasdfsdf1111111111111111111111111111111111";
		//Integer x = 255;
		for(long i = -123123123L ; i <60000000L;i++) {
			Long a11b = getVal(getBytes(i));
			if(a11b.intValue()!=i)throw new Exception(i+"");
		}
		System.out.println(System.currentTimeMillis()-time);
		
		
		time = System.currentTimeMillis();
		//String x = "fasdfasdfsdf1111111111111111111111111111111111";
		//Integer x = 255;
		for(long i = -123123123L ; i <60000000L;i++) {

			Long a11b = getValFromSerialized(getSerializedBytes(i));
			if(a11b.longValue()!=i) {
				throw new Exception(i+"");
			}
		}
		System.out.println(System.currentTimeMillis()-time);
		
		/*time =System.currentTimeMillis();
    	for(int i = 0 ; i <600000;i++) {
    		SerializationUtil.deserialize(SerializationUtil.serialize(x),Integer.class);
    	}
    	*/
    	
	}
	
}
