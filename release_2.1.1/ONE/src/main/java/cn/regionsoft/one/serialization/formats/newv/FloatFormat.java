package cn.regionsoft.one.serialization.formats.newv;

import cn.regionsoft.one.serialization.formats.core.SeriziDataType;

public class FloatFormat {
	public static final byte DATATYPE = (byte) SeriziDataType.Float.ordinal();
	
	public static byte[] getSerializedBytes(Float para) {
		byte[] src = getBytes(para);
		byte[] result = new byte[src.length+1];
		result[0] = DATATYPE;
		System.arraycopy(src, 0, result, 1, src.length);
		return result;
	}
	
	public static Float getValFromSerialized(byte[] bytes) {
		byte[] result = new byte[bytes.length-1];
		System.arraycopy(bytes, 1, result, 0, result.length);
		return getVal(result);
	}
	
	public static byte[] getBytes(Float para) {
		String tmp = String.valueOf(para);
		return null;//return StringFormat.getBytes(tmp);
	}
	
	public static Float getVal(byte[] bytes) {
		//String tmp = StringFormat.getVal(bytes);
		return null;//return Float.valueOf(tmp);
	}

}
