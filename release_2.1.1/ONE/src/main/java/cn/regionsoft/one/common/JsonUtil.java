package cn.regionsoft.one.common;

import cn.regionsoft.one.core.CommonUtil;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class JsonUtil {
    public static ObjectMapper readMapper = new ObjectMapper();
    public static ObjectMapper writerMapper = new ObjectMapper();
    
    static {
    	 readMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
         readMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
         readMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    	
         writerMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
         writerMapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
         writerMapper.setSerializationInclusion(Include.NON_NULL);
         writerMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    public static <T> T parseToObject(InputStream is, Class<T> toClass) {
        try {
            return readMapper.readValue(is, toClass);
        } catch (Exception var3) {
            var3.printStackTrace();
            throw new RuntimeException(var3);
        }
    }

    public static <T> T parseToObject(byte[] b, int offset, int len, Class<T> valueType) {
        try {
            return readMapper.readValue(b, offset, len, valueType);
        } catch (Exception var5) {
            var5.printStackTrace();
            throw new RuntimeException(var5);
        }
    }

    public static <T> T parseToObject(String json, Class<T> toClass) {
        try {
            return CommonUtil.isEmpty(json)?null:readMapper.readValue(json, toClass);
        } catch (Exception var3) {
            var3.printStackTrace();
            throw new RuntimeException(var3);
        }
    }

    public static String getNodeValue(String json, String fieldName) {
        try {
            if(CommonUtil.isEmpty(json)) {
                return null;
            } else if(fieldName == null) {
                return json;
            } else {
                JsonNode node = readMapper.readTree(json);
                JsonNode fnode = node.path(fieldName);
                return fnode.toString();
            }
        } catch (Exception var4) {
            var4.printStackTrace();
            throw new RuntimeException(var4);
        }
    }

    public static <T> T parseToObject(String json, String fieldName, Class<T> toClass) {
        return parseToObject(getNodeValue(json, fieldName), toClass);
    }

    public static Map parseToMap(String json) {
        return (Map)parseToObject(json, Map.class);
    }

    public static Map parseToMap(byte[] b) {
        return b != null && b.length != 0?(Map)parseToObject(b, 0, b.length, Map.class):null;
    }

    public static Map parseToMap(InputStream is) {
        return (Map)parseToObject(is, Map.class);
    }

    public static Map parseToMap(Object o) {
        String oJson = parseToJson(o);
        return (Map)parseToObject(oJson, Map.class);
    }

    public static String parseToJson(Object o) {
        return parseToJson(o, false);
    }

    public static String parseToJson(Object o, boolean ignoreNull) {
        if(o == null) {
            return null;
        } else {
            try {
                return writerMapper.writeValueAsString(o);
            } catch (Exception var4) {
                var4.printStackTrace();
                throw new RuntimeException(var4);
            }
        }
    }

    

	public static <T> String listToJson(List<T> list) {
		try {
			String jsonStr = readMapper.writeValueAsString(list);
			return jsonStr;
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> List<T> jsonToList(String json, Class<T> clz) {
		try {
			JavaType javaType = getCollectionType(ArrayList.class, clz); 
			List<T> result =  readMapper.readValue(json, javaType);
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {   
		return readMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);   
	}

	//------------------------------
	public static <T> T jsonToBean(String json, Class<T> toClass) {
		return parseToObject(json, toClass);
	}
	
	public static <T> T jsonToBeanDateSerializer(String json, Class<T> toClass, String dateFormat1) {
		return parseToObject(json, toClass);
	}
	
	public static String objectToJson(Object object) {
		return parseToJson(object);
	}
	
	
	public static String objectToJsonDateSerializer(Object object, String dateFormat1) {
		return parseToJson(object);
	}

	public static Object[] jsonToArray(String json) {
		return (Object[])parseToObject(json, Object[].class);
	}


	public static Map jsonToMap(String json) {
		return (Map)parseToObject(json, Map.class);
	}

	
}
