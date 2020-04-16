package cn.regionsoft.one.rpc.common;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

/**
 * 序列化工具类（基于 Protostuff 实现）
 *
 * @author  
 * @since 1.0.0
 */
public class SerializationUtil {
    private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap();

    private static Objenesis objenesis = new ObjenesisStd(true);
    
    private static ThreadLocal<LinkedBuffer> threadLinkedBuffer = new ThreadLocal<LinkedBuffer>();

    private SerializationUtil() {
    }

    @SuppressWarnings("unchecked")
    private static <T> Schema<T> getSchema(Class<T> cls) {
        Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
        if (schema == null) {
            schema = RuntimeSchema.createFrom(cls);
            if (schema != null) {
                cachedSchema.put(cls, schema);
            }
        }
        return schema;
    }

    /**
     * 序列化（对象 -> 字节数组）
     */
    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = threadLinkedBuffer.get();
        if(buffer==null) {
        	buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        	threadLinkedBuffer.set(buffer);
        }
        else{
        	buffer.clear();
        }
        
        try {
            Schema<T> schema = getSchema(cls);
            return ProtobufIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } 
    }

    /**
     * 反序列化（字节数组 -> 对象）
     */
    public static <T> T deserialize(byte[] data, Class<T> cls) {
        try {
            T message = (T) objenesis.newInstance(cls);
            //T message = (T) cls.newInstance();
            Schema<T> schema = getSchema(cls);
            ProtobufIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
    
    public static <T> T deserializeFromStream(InputStream in, Class<T> cls) {
        try {
            T message = (T) objenesis.newInstance(cls);
            //T message = (T) cls.newInstance();
            Schema<T> schema = getSchema(cls);
            ProtobufIOUtil.mergeFrom(in, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
    
    public static <T> void serializeToStream(T obj,OutputStream out) {
        Class<T> cls = (Class<T>) obj.getClass();
        LinkedBuffer buffer = threadLinkedBuffer.get();
        if(buffer==null) {
        	buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        	threadLinkedBuffer.set(buffer);
        }
        else{
        	buffer.clear();
        }
        try {
            Schema<T> schema = getSchema(cls);
            ProtobufIOUtil.writeTo(out, obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } 
    }
    
    public static void main(String[] args) throws InstantiationException, IllegalAccessException {
    	RpcRequest a = new RpcRequest();
    	a.setRegisterPath("sadfasdf");
    	a.setMethodName("sdfasdfsdf");
    	//a.setParameters(new Object[] {"asdasd"});
    	long time =System.currentTimeMillis();
    	for(int i = 0 ; i <600000;i++) {
    		SerializationUtil.deserialize(SerializationUtil.serialize(a),RpcRequest.class);
    	}
    	System.out.println(System.currentTimeMillis()-time);
    	/*time =System.currentTimeMillis();
    	for(int i = 0 ; i <1000000;i++) {
    		objenesis.newInstance(RpcRequest.class);
    	}
    	System.out.println(System.currentTimeMillis()-time);*/
    }
}
