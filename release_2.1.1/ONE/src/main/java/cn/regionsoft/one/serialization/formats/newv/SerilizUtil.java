package cn.regionsoft.one.serialization.formats.newv;

import io.netty.buffer.ByteBuf;

public class SerilizUtil {
	private static SeriliWorker seriliWorker = new SeriliWorker();
	/*SeriliWorker worker = seriliManager.getWorker();
	worker.getVal(bytes, targetClass)*/
	
	public static <T> ByteBuf serialize(T obj) {
		try {
			return seriliWorker.serialize(obj);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static <T> ByteBuf serialize(T obj,ByteBuf byteBuf) {
		try {
			return seriliWorker.serialize(obj,byteBuf);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Object deserialize(ByteBuf byteBuf, Class<?> class1) {
		try {
			return seriliWorker.deserialize(byteBuf,class1);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
