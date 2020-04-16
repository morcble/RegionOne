package cn.regionsoft.one.serialization.formats.newv;

import cn.regionsoft.one.serialization.formats.core.SeriziDataType;

public class DoubleFormat {
	public static final byte DATATYPE = (byte) SeriziDataType.Double.ordinal();
	
	public static byte[] getSerializedBytes(Double para) {
		byte[] src = getBytes(para);
		byte[] result = new byte[src.length+1];
		result[0] = DATATYPE;
		System.arraycopy(src, 0, result, 1, src.length);
		return result;
	}
	
	public static Double getValFromSerialized(byte[] bytes) {
		byte[] result = new byte[bytes.length-1];
		System.arraycopy(bytes, 1, result, 0, result.length);
		return getVal(result);
	}
	
	public static byte[] getBytes(Double para) {
		String tmp = String.valueOf(para);
		return null;//return StringFormat.getBytes(tmp);
	}
	
	public static Double getVal(byte[] bytes) {
//String tmp = StringFormat.getVal(bytes);
		return null;//return Double.valueOf(tmp);
	}

}
