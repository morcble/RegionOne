package cn.regionsoft.one.serialization.formats.newv;

import java.nio.charset.Charset;
import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.serialization.formats.core.SeriziDataType;
import io.netty.buffer.ByteBuf;

/**
 * lengthByte  -length - content
 * @author fenglj
 *
 */
public class StringFormat{
	public static final byte DATATYPE = (byte) SeriziDataType.String.ordinal();
	public static final Charset CHARSETUTF8 = Charset.forName(Constants.UTF8);
	public static void wrapBytes(String para, ByteBuf byteBuf) {
		int initIndex = byteBuf.readableBytes();
		
		byteBuf.writeByte(DATATYPE);
		byteBuf.writeIntLE(0);
		if(para!=null) {
			int length = byteBuf.writeCharSequence(para, CHARSETUTF8);
			byteBuf.setIntLE(initIndex+1, length);
		}
	}
	
	public static ByteBuf getBytes(String para, ByteBuf byteBuf) {
		byteBuf.writeByte(DATATYPE);
		byteBuf.writeIntLE(0);
		if(para!=null) {
			int length = byteBuf.writeCharSequence(para, CHARSETUTF8);
			byteBuf.setIntLE(1, length);
		}

		return byteBuf;
	}



	public static String getValFromSerialized(ByteBuf byteBuf,int skipBytes,int length) {
		if(skipBytes>0)byteBuf.skipBytes(skipBytes);
		byte[] result = new byte[length];
		byteBuf.readBytes(result);
		
		return new String(result,CHARSETUTF8);
	}

}
