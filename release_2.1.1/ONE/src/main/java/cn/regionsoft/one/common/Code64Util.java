package cn.regionsoft.one.common;
import java.util.HashMap;
import java.util.Map;

/**
 * 把long和64位编码相互转换
 */
public class Code64Util {
	private static final char[] CODES = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ[]abcdefghijklmnopqrstuvwxyz".toCharArray();
	private static final Map<Integer,Integer> CODE_INDEX_MAP = new HashMap<Integer,Integer>();
	
	static {
		for(int i = 0 ; i <CODES.length ; i++) {
			CODE_INDEX_MAP.put((int) CODES[i], i);
		}
	}
	
	
	public static void main(String[] args) {
		long x = 4652032700688891905L;
		String val = longToStr64(x);
		System.out.println(val);
		System.out.println(str64ToLong(val));
	}
	
	public static String longToStr64(long x) {
		StringBuilder resultStrBuilder = new StringBuilder();
		while(x>0) {
			resultStrBuilder.append(CODES[(int) (x & 0b111111L)]);
			x = x >>6;
		}
		resultStrBuilder.reverse();
		return resultStrBuilder.toString();
	}
	
	private static long str64ToLong(String str64) {
		long result = 0L;
		char[] chars = str64.toCharArray();
		for(int i = 0 ; i <chars.length ; i ++) {
			result = (result<<6)+CODE_INDEX_MAP.get((int)chars[i]);
		}
		return result;
	}
}
