package cn.regionsoft.one.serialization.formats.newv;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.regionsoft.one.rpc.common.SerializationUtil;
import cn.regionsoft.one.serialization.formats.core.SeriziDataType;

import io.netty.buffer.ByteBuf;

/**
 * java class format 
 * @author fenglj
 *
 */
public class Test2 {
	public static void main(String[] args) throws InterruptedException {
		TestBean tb = new TestBean();
		tb.setName("a12312312132222222222222222222222222222222222222222222222222223dfhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
		tb.setDesc("a12312323");
		
		
		TestBean2 tb2 = new TestBean2();
		tb2.setName("a2asdasdsd");
		tb2.setDesc("a2sdsdsd");
		
		tb.setTestBean2(tb2);
		
		long time ;
		for(int i = 0 ; i <1000000;i++) {
			ByteBuf a = SerilizUtil.serialize(tb);
			byte[] b = SerializationUtil.serialize(tb);
			
			Object a1 = SerializationUtil.deserialize(b, TestBean.class);
			Object b1 = SerilizUtil.deserialize(a, TestBean.class);
			//System.out.println(1);
		}
		
		
		System.gc();

		time =System.currentTimeMillis();
		for(int i = 0 ; i <3000000;i++) {
			SerializationUtil.serialize(tb);
			//SerializationUtil.deserialize(SerializationUtil.serialize(tb),TestBean.class);
		}
		System.out.println(System.currentTimeMillis()-time);
		
		System.gc();
		
		time =System.currentTimeMillis();
		for(int i = 0 ; i <3000000;i++) {
			SerilizUtil.serialize(tb);
			//SerilizUtil.deserialize(SerilizUtil.serialize(tb),TestBean.class);

		}
		System.out.println(System.currentTimeMillis()-time);
		System.gc();
		
		time =System.currentTimeMillis();
		for(int i = 0 ; i <3000000;i++) {
			//SerializationUtil.serialize(tb);
			SerializationUtil.deserialize(SerializationUtil.serialize(tb),TestBean.class);
		}
		System.out.println(System.currentTimeMillis()-time);
		
		System.gc();
		time =System.currentTimeMillis();
		for(int i = 0 ; i <3000000;i++) {
			//SerilizUtil.serialize(tb);
			SerilizUtil.deserialize(SerilizUtil.serialize(tb),TestBean.class);

		}
		System.out.println(System.currentTimeMillis()-time);
		
	}
	
}
