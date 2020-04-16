package cn.regionsoft.one.rpc.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder {
    private Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }
    
    static boolean useRegionSerilization = false;
    @Override
    public void encode(ChannelHandlerContext ctx, Object obj, ByteBuf out) throws Exception {
    	out.writeInt(0);
    	ByteBufOutputStream bos = new ByteBufOutputStream(out);
    	SerializationUtil.serializeToStream(obj, bos);
    	bos.close();
    	
    	int totalLength = out.readableBytes();
    	out.markWriterIndex();//标记
    	out.writerIndex(0);
    	out.writeInt(totalLength-4);
    	out.resetWriterIndex();
    }
}