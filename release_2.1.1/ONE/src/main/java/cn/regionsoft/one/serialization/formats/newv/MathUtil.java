package cn.regionsoft.one.serialization.formats.newv;

public class MathUtil {
	public static byte[] unsignIntToBytes2(int intVal) {
		if(intVal<0) throw new RuntimeException("minus value is not allowed :"+intVal);
		if(intVal==0)return new byte[] {-128};
		
		if(intVal<256) {
			return new byte[] {(byte) (intVal-128)};
		}
		
		int sizeOfByte;
		double tmpFloat = Math.log(intVal)/Math.log(256);
		double k = Math.ceil(tmpFloat);
		if(k==tmpFloat) {
			sizeOfByte=(int) (k+1);
		}
		else {
			sizeOfByte = (int) k;
		}
		byte[] lengthByte = new byte[sizeOfByte];
		int x;
		int y;
		for(int i = 0 ; i <sizeOfByte;i++) {
			x = intVal/256;
			y = intVal%256;
			lengthByte[sizeOfByte-1-i] = (byte) (y-128);
			intVal = x;
		}
		return lengthByte;
	}
	
	public static int bytesToInt2(byte[] data, int readerIndex) {
		int result = 0;
		for(int i = readerIndex; i<readerIndex+4;i++) {
			result = result<<8;
			result+= (data[i]+128);
		}
		return result;
	}
	
	
	
	//---------------------------------------------------
	public static byte[] intToBytes(int intVal) {
		if(intVal<0) {
			intVal = -intVal;
			if(intVal<256) {
				return new byte[] {1,(byte) (intVal-128)};
			}
			
			int sizeOfByte;
			double tmpFloat = Math.log(intVal)/Math.log(256);
			double k = Math.ceil(tmpFloat);
			if(k==tmpFloat) {
				sizeOfByte=(int) (k+1);
			}
			else {
				sizeOfByte = (int) k;
			}
			
			sizeOfByte++;
			byte[] lengthByte = new byte[sizeOfByte];
			lengthByte[0] = 1;
			int x;
			int y;
			for(int i = 1 ; i <sizeOfByte;i++) {
				x = intVal/256;
				y = intVal%256;
				lengthByte[sizeOfByte-i] = (byte) (y-128);
				intVal = x;
			}
			return lengthByte;
		}
		else {
			return unsigneIntToBytes(intVal);
		}
	}
	
	private static byte[] unsigneIntToBytes(int intVal) {
		if(intVal<0) throw new RuntimeException("minus value is not allowed :"+intVal);
		if(intVal==0)return new byte[] {0,-128};
		
		if(intVal<256) {
			return new byte[] {0,(byte) (intVal-128)};
		}
		
		int sizeOfByte;
		double tmpFloat = Math.log(intVal)/Math.log(256);
		double k = Math.ceil(tmpFloat);
		if(k==tmpFloat) {
			sizeOfByte=(int) (k+1);
		}
		else {
			sizeOfByte = (int) k;
		}
		sizeOfByte++;
		byte[] lengthByte = new byte[sizeOfByte];
		lengthByte[0] = 0;
		int x;
		int y;
		for(int i = 1 ; i <sizeOfByte;i++) {
			x = intVal/256;
			y = intVal%256;
			lengthByte[sizeOfByte-i] = (byte) (y-128);
			intVal = x;
		}
		return lengthByte;
	}
	
	/*
	 * [<s>123123]<end>
	 * @param bytes
	 * @param startIndex
	 * @param endIndex
	 * @return
	 */
	public static int bytesToInt(byte[] bytes,int startIndex,int endIndex) {
		if(bytes==null)throw new RuntimeException("bytes is null");
		int result = 0;
		int sign = bytes[startIndex];
		for(int i = startIndex+1 ; i <endIndex;i++) {
			result = result<<8;
			result+= (bytes[i]+128);
		}
		if(sign==0)
			return result;
		else 
			return -result;
	}
//---------------------------------------------------------------------------------------------------------
	public static long bytesToLong(byte[] bytes, int startIndex, int endIndex) {
		if(bytes==null)throw new RuntimeException("bytes is null");
		long result = 0;
		int sign = bytes[startIndex];
		for(int i = startIndex+1 ; i <endIndex;i++) {
			result = result<<8;
			result+= (bytes[i]+128);
		}
		if(sign==0)
			return result;
		else 
			return -result;
	}

	public static byte[] longToBytes(long longVal) {
		if(longVal<0) {
			longVal = -longVal;
			if(longVal<256) {
				return new byte[] {1,(byte) (longVal-128)};
			}
			
			int sizeOfByte;
			double tmpFloat = Math.log(longVal)/Math.log(256);
			double k = Math.ceil(tmpFloat);
			if(k==tmpFloat) {
				sizeOfByte=(int) (k+1);
			}
			else {
				sizeOfByte = (int) k;
			}
			
			sizeOfByte++;
			byte[] lengthByte = new byte[sizeOfByte];
			lengthByte[0] = 1;
			long x;
			long y;
			for(int i = 1 ; i <sizeOfByte;i++) {
				x = longVal/256;
				y = longVal%256;
				lengthByte[sizeOfByte-i] = (byte) (y-128);
				longVal = x;
			}
			return lengthByte;
		}
		else {
			return unsigneLongToBytes(longVal);
		}
	}
	
	private static byte[] unsigneLongToBytes(long longVal) {
		if(longVal<0) throw new RuntimeException("minus value is not allowed :"+longVal);
		if(longVal==0)return new byte[] {0,-128};
		
		if(longVal<256) {
			return new byte[] {0,(byte) (longVal-128)};
		}
		
		int sizeOfByte;
		double tmpFloat = Math.log(longVal)/Math.log(256);
		double k = Math.ceil(tmpFloat);
		if(k==tmpFloat) {
			sizeOfByte=(int) (k+1);
		}
		else {
			sizeOfByte = (int) k;
		}
		sizeOfByte++;
		byte[] lengthByte = new byte[sizeOfByte];
		lengthByte[0] = 0;
		long x;
		long y;
		for(int i = 1 ; i <sizeOfByte;i++) {
			x = longVal/256;
			y = longVal%256;
			lengthByte[sizeOfByte-i] = (byte) (y-128);
			longVal = x;
		}
		return lengthByte;
	}
//---------------------------------------------------------------------------------------------------------



	
	
}
