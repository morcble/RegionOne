package cn.regionsoft.one.serialization.formats.newv;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class TestReadInt {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		byte[] test1 = new byte[2];
		ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer(10);
		
		byteBuf.writeIntLE(556);

		
		byte[] a = MathUtil.intToBytes(556);
		long time ;
		for(int i = 0 ; i <10000000;i++) {
			MathUtil.bytesToInt(a, 0, a.length);
			
			int aa = byteBuf.readIntLE();
			byteBuf.readerIndex(0);
		}
		
		
		time =System.currentTimeMillis();
		for(int i = 0 ; i <10000000;i++) {
		 //MathUtil.intToBytes(556);
			MathUtil.bytesToInt(a, 0, a.length);
		}
		System.out.println(System.currentTimeMillis()-time);
		
		
		time =System.currentTimeMillis();
		for(int i = 0 ; i <10000000;i++) {
			 byteBuf.readIntLE();
			byteBuf.readerIndex(0);
			
			
		}
		System.out.println(System.currentTimeMillis()-time);
		
		

	}

}
