package cn.regionsoft.one.common;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cn.regionsoft.one.core.CommonUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
//import com.google.gson.internal.LinkedTreeMap;
 

public class GsonUtil{
 
    private static Gson gson=null;
    static{
        if(gson==null){
            gson=new Gson();
        }
    }
    private GsonUtil(){}
    /**
     * 将对象转换成json格式
     * @param ts
     * @return
     */
    public static String objectToJson(Object ts){
        String jsonStr=null;
        if(gson!=null){
            jsonStr=gson.toJson(ts);
        }
        return jsonStr;
    }
    /**
     * 将对象转换成json格式(并自定义日期格式)
     * @param ts
     * @return
     */
    public static String objectToJsonDateSerializer(Object ts,final String dateformatPattern){
        String jsonStr=null;
        final SimpleDateFormat format = CommonUtil.getSimpleDateFormat(dateformatPattern);
        gson=new GsonBuilder().registerTypeHierarchyAdapter(Date.class, new JsonSerializer<Date>() {
            public JsonElement serialize(Date src, Type typeOfSrc,
                    JsonSerializationContext context) {
                return new JsonPrimitive(format.format(src));
            }
        }).setDateFormat(dateformatPattern).create();
        if(gson!=null){
            jsonStr=gson.toJson(ts);
        }
        return jsonStr;
    }
    
    /**
     * 将json格式转换成list对象
     * @param jsonStr
     * @return
     */
    public static <T> List<T> jsonToList(String jsonStr,Class<T> classType){
    	List<T> result = new ArrayList();
        List<?> objList=null;
        if(gson!=null){
            java.lang.reflect.Type type=new com.google.gson.reflect.TypeToken<List<?>>(){}.getType();
            objList=gson.fromJson(jsonStr, type);
        }
        if(classType!=null){
        	for(Object a:objList){
        		Map ltm = (Map)a;
        		Object tmp = null;
				try {
					tmp = wrapLinkedTreeMap(ltm,classType);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(tmp!=null)result.add((T) tmp);
        	}
        }
        return result;
    }
    public static Object wrapLinkedTreeMap(Map src,Class classType) throws Exception{
    	return wrapLinkedTreeMap(src,classType,Constants.DATE_FORMAT1);
    }
    
    public static Object wrapLinkedTreeMap(Map src,Class classType,String dateFormat) throws Exception{
    	Map ltm = (Map)src;
		try {
			Object classInstance = classType.newInstance();
			Class superClass = classType.getSuperclass();
			while(superClass!=Object.class && superClass!=null) {
				Field[] filedsOfSuperClass = superClass.getDeclaredFields();
				for(Field f:filedsOfSuperClass){
					handleField(ltm,f,classInstance,dateFormat);
				}
				superClass = superClass.getSuperclass();
			}
			
			Field[] fileds = classType.getDeclaredFields();
			for(Field f:fileds){
				handleField(ltm,f,classInstance,dateFormat);
			}
			
			return classInstance;
		} catch (Exception e) {
			throw e;
		} 
		
    }
    
    private static void handleField(Map ltm,Field f,Object classInstance,String dateFormat) throws NumberFormatException, IllegalArgumentException, IllegalAccessException, ParseException, Exception {
    	SimpleDateFormat sdf = CommonUtil.getSimpleDateFormat(dateFormat);
    	if(ltm.containsKey(f.getName())){
			f.setAccessible(true);
			Object filedVal = ltm.get(f.getName());

			if(f.get(classInstance) instanceof List){
				List tR = new ArrayList();
				for(Map x:(List<Map>) filedVal){
					tR.add( wrapLinkedTreeMap(x,(Class) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0]));
				}
				f.set(classInstance, tR);
			}
			else{
				if(f.getType().equals(String.class)){
					if(filedVal.getClass().equals(Long.class)){
						f.set(classInstance,String.valueOf(filedVal));
					}
					else if(filedVal.getClass().equals(Double.class)){
						long l = Math.round((Double)filedVal);  
						f.set(classInstance,String.valueOf(l));
					}
					else{
						f.set(classInstance, filedVal);
					}
				}
				else if(f.getType().equals(Integer.class)){
					Object value = filedVal;
					
					if(value.getClass().equals(Double.class)){
						f.set(classInstance,((Double)filedVal).intValue());//
					}
					else if(value.getClass().equals(Integer.class)){
						f.set(classInstance,filedVal);
					}
					else if(value.getClass().equals(Long.class)){
						f.set(classInstance,((Long)filedVal).intValue());
					}
					else{
						f.set(classInstance, filedVal);
					}
				}
				else if(f.getType().equals(Long.class)){
					Object value = filedVal;
					if(value.getClass().equals(Double.class)){
						f.set(classInstance,Long.parseLong(String.valueOf(Math.round((Double)filedVal))));
					}
					else if(value.getClass().equals(Long.class)){
						f.set(classInstance, filedVal);
					}
					else if(value.getClass().equals(String.class)){
						f.set(classInstance, Long.valueOf((String)value));
					}
					else{
						f.set(classInstance, filedVal);
					}
				}
				
				else if(f.getType().equals(Date.class)){
					f.set(classInstance,sdf.parse((String) filedVal));
				}
				else{
					f.set(classInstance, filedVal);
				}
			}
		}
    }
    
    /*@Deprecated
    public static List<?> jsonToList(String jsonStr){
        List<?> objList=null;
        if(gson!=null){
            java.lang.reflect.Type type=new com.google.gson.reflect.TypeToken<List<?>>(){}.getType();
            objList=gson.fromJson(jsonStr, type);
        }
        return objList;
    }
    */
    public static  Object[] jsonToArray(String jsonStr){
    	Object[] objMap=null;
        if(gson!=null){
            java.lang.reflect.Type type=new com.google.gson.reflect.TypeToken<Object[]>(){}.getType();
            objMap=gson.fromJson(jsonStr, type);
        }
        return objMap;
    }
    /**
     * 将json格式转换成map对象
     * @param jsonStr
     * @return
     */
    public static <T,X> Map<T,X> jsonToMap(String jsonStr){
        Map<T,X> objMap=null;
        if(gson!=null){
            java.lang.reflect.Type type=new com.google.gson.reflect.TypeToken<Map<T,X>>(){}.getType();
            objMap=gson.fromJson(jsonStr, type);
        }
        return objMap;
    }
    /**
     * 将json转换成bean对象
     * @param jsonStr
     * @return
     */
    public static <T> T jsonToBean(String jsonStr,Class<T> cl){
        Object obj=null;
        if(gson!=null){
            obj=gson.fromJson(jsonStr, cl);
        }
        return (T) obj;
    }
    /**
     * 将json转换成bean对象
     * @param jsonStr
     * @param cl
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T  jsonToBeanDateSerializer(String jsonStr,Class<T> cl,final String pattern){
        Object obj=null;
        gson=new GsonBuilder().registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            public Date deserialize(JsonElement json, Type typeOfT,
                    JsonDeserializationContext context)
                    throws JsonParseException {
                    SimpleDateFormat format=CommonUtil.getSimpleDateFormat(pattern);
                    String dateStr=json.getAsString();
                try {
                    return format.parse(dateStr);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }).setDateFormat(pattern).create();
        if(gson!=null){
            obj=gson.fromJson(jsonStr, cl);
        }
        return (T)obj;
    }
    /**
     * 根据
     * @param jsonStr
     * @param key
     * @return
     */
    public static Object  getJsonValue(String jsonStr,String key){
        Object rulsObj=null;
        Map<?,?> rulsMap=jsonToMap(jsonStr);
        if(rulsMap!=null&&rulsMap.size()>0){
            rulsObj=rulsMap.get(key);
        }
        return rulsObj;
    }
    
 
     
}