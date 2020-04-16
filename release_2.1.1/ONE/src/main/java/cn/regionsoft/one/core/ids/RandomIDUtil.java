package cn.regionsoft.one.core.ids;

import java.security.SecureRandom;
import java.util.Random;

public class RandomIDUtil {
	private static char[] chars = "1234567890abcdefghijklmnopqrstuvwxyz".toCharArray();
	private static Random random = new SecureRandom();
	private static int charsAmount = chars.length;
	public static void nextId(int length) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i <length ; i ++) {
			sb.append(chars[random.nextInt(charsAmount)]);
		}
	}

	public static void main(String[] args) {
		for(int i = 0 ; i<1000;i++) {
			RandomIDUtil.nextId(6);
		}
	}

}
