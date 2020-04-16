package cn.regionsoft.one.serialization.formats.newv;

import cn.regionsoft.one.rpc.common.SerializationUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class Test {
	private static ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(128);
	public static void main(String[] args) throws Exception {
		long time ;
		TestBean tb = new TestBean();
		tb.setBb(new byte[] {1,2});
		tb.setName("a");
		tb.setDesc("a");
		
		
		TestBean2 tb2 = new TestBean2();
		tb2.setName("a2");
		tb2.setDesc("a2");
		
		tb.setTestBean2(tb2);
		
		SeriliWorker sw = new SeriliWorker();
		ByteBuf bytes = sw.serialize(tb);
		TestBean testBean = (TestBean) sw.deserialize(bytes, TestBean.class);
		
		System.out.println(1);
	}

}
