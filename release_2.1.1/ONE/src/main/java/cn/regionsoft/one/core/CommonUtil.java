package cn.regionsoft.one.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.text.StrSubstitutor;

import cn.regionsoft.one.annotation.InstanceAnoType;
import cn.regionsoft.one.annotation.NoInstanceAnoType;
import cn.regionsoft.one.caches.LocalCacheUtil;
import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.common.JsonUtil;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.aop.AOPListener;
import cn.regionsoft.one.core.auth.dto.RequestInfoDto;
import cn.regionsoft.one.core.entity.BindColumn;
import cn.regionsoft.one.core.entity.BindObject;
import cn.regionsoft.one.data.dialet.core.SQLDialet;
import cn.regionsoft.one.data.persistence.H2OEntity;
import cn.regionsoft.one.data.persistence.Text;
import cn.regionsoft.one.enums.LocaleStr;
import cn.regionsoft.one.reflect.MethodMeta;
import cn.regionsoft.one.reflect.enums.ReturnClassType;
import cn.regionsoft.one.standalone.RegionHttpRequest;
import cn.regionsoft.one.standalone.RegionHttpResponse;
import cn.regionsoft.one.standalone.fileupload.RegionFileItem;
import cn.regionsoft.one.standalone.fileupload.UploadResolver;
import cn.regionsoft.one.utils.ThreadData;
import cn.regionsoft.one.utils.ThreadHolder;
import cn.regionsoft.one.web.wrapper.WebReqWrapper;
import com.google.common.collect.Maps;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;


public class CommonUtil {
	private static final Logger logger = Logger.getLogger(CommonUtil.class);
	
	public static <T extends Annotation> T getFieldAnnotation(Field field,Class<T> annotationClass){
		return field.getDeclaredAnnotation(annotationClass);
	}
	
	public static WebReqWrapper resolveWebReqWrapper(String requestStr){
		WebReqWrapper webReqWrapper = (WebReqWrapper) JsonUtil.jsonToBean(requestStr, WebReqWrapper.class);
		if(webReqWrapper==null) return null;
		RequestInfoDto requestInfoDto = RequestInfoHolder.getInstance().getRequestInfo();
		requestInfoDto.setRequestId(webReqWrapper.getRequestId());
		
		//for cloud begin
		requestInfoDto.setRsAppId(webReqWrapper.getRsAppId());
		//for cloud end
		
		return webReqWrapper;
	}
	
	/**
	 * 获取时间格式类
	 * @param pattern
	 * @param localeStr
	 * @return
	 */
	public static SimpleDateFormat getSimpleDateFormat(String pattern,LocaleStr localeStr) {
		ThreadHolder threadHolder =  ThreadHolder.getInstance();
		ThreadData threadData = threadHolder.getThreadDatas().get();
		if(threadData == null) {
			threadData = new ThreadData();
			threadHolder.getThreadDatas().set(threadData);
		}
		return threadData.getSimpleDateFormat(pattern, localeStr.name());
	}
	
	/**
	 * 获取时间格式类
	 * @param pattern
	 * @param localeStr
	 * @return
	 */
	public static SimpleDateFormat getSimpleDateFormat(String pattern) {
		return getSimpleDateFormat(pattern,LocaleStr.cn);//TODO get locale from request
	}
	
	public static String boolToStr(Boolean value){
		if(value==null) return null;
		if(value) return "1";
		else return "0";
	}
	
	public static Boolean strToBool(String value){
		if(value==null) return null;
		if(value.equals("1")) return true;
		else return false;
	}
	
	public static String longToStr(Long value){
		if(value==null) return null;
		return String.valueOf(value);
	}
	
	public static Long strToLong(String value){
		if(value==null) return null;
		return Long.valueOf(value);
	}
	
	public static <T extends Enum> T getEnumByName(String name,Class<T> ennumClass){
		T[] arrays = ennumClass.getEnumConstants();
		for(T tmp:arrays){
			if(tmp.name().equals(name))
				return tmp;
		}
		return null;
	}
	
	public static Properties loadProperties(File tmp) throws Exception {
		Properties newP = new Properties();
		InputStream is = null;
		try{
			is = new FileInputStream(tmp);
			newP.load(is);
			return newP;
		}
		catch(Exception e){
			throw e;
		}
		finally{
			closeQuietly(is);
		}
	}
	
	public static Properties loadProperties(InputStream is) throws Exception {
		Properties newP = new Properties();
		try{
			newP.load(is);
			return newP;
		}
		catch(Exception e){
			throw e;
		}
		finally{
			closeQuietly(is);
		}
	}
	

	
	private final static String EMPTY_STR="";
	public static boolean isEmpty(String str){
		if(str==null||str.trim().equals(EMPTY_STR)){
			return true;
		}
		return false;
	}
	
	public static boolean isEmpty(Object source){
		if(source==null){
			return true;
		}
		else return false;
	}
	
	public static boolean isNotEmpty(String source){
		return !isEmpty(source);
	}
	
	private static Annotation getInstanceAnoType(Class<?> classType){
		Annotation annotation = null;
		for (InstanceAnoType s : InstanceAnoType.values())  {
			annotation = classType.getDeclaredAnnotation(s.getClassType());
			if(annotation!=null){
				return annotation;
			}
		}
		
		Class<?> parentClass = classType.getSuperclass();
		if(parentClass!=Object.class && parentClass!=null){
			return getInstanceAnoType(parentClass);
		}
		else{
			return null;
		}
	}
	
	private static Annotation getNoInstanceAnoType(Class<?> classType){
		Annotation annotation = null;
		for (NoInstanceAnoType s : NoInstanceAnoType.values())  {
			annotation = classType.getDeclaredAnnotation(s.getClassType());
			if(annotation!=null){
				return annotation;
			}
		}
		
		Class<?> parentClass = classType.getSuperclass();
		if(parentClass!=Object.class && parentClass!=null){
			return getNoInstanceAnoType(parentClass);
		}
		else{
			return null;
		}
	}
	
	/**
	 * get xframecontext for this class
	 * @param classType
	 * @return
	 */
	private static final String targetContextStr = "targetContext";
	public static String getTargetContextName(Class<?> classType){
		
		
		Annotation annotation = getInstanceAnoType(classType);
		if(annotation==null) {
			annotation = getNoInstanceAnoType(classType);
		}
		if(annotation==null)return null;

		Method[] methods = annotation.getClass().getDeclaredMethods();
		for(Method tmp :methods ){
			if(tmp.getName().equals(targetContextStr)){
				try {
					return (String) tmp.invoke(annotation);
				} catch (Exception e) {
					logger.error(e);
				} 
			}
		}
		return null;
	}
	
	
	/**
	 * @param classType
	 * @return
	 */
	
	public static H2OContext getTargetContext(Class<?> classType){
		String cacheKey = LocalCacheUtil.GET_TARGET_CONTEXT+classType.getName();
    	Object cached = LocalCacheUtil.get(cacheKey);
    	if(cached!=null) {
    		return (H2OContext) cached;
    	}
		
		String contextName = getTargetContextName(classType);
		H2OContext h2oContext = SystemContext.getInstance().getContext(contextName);
		
		LocalCacheUtil.put(cacheKey, h2oContext);
		return h2oContext;
	}
	
	public static H2OContext getTargetContextByClassName(String classFullName) throws ClassNotFoundException{
		String cacheKey = LocalCacheUtil.GET_TARGET_CONTEXT+classFullName;
    	Object cached = LocalCacheUtil.get(cacheKey);
    	if(cached!=null) {
    		return (H2OContext) cached;
    	}
		
    	Class<?> classType = Class.forName(classFullName);
  
    	String contextName = getTargetContextName(classType);
		H2OContext h2oContext = SystemContext.getInstance().getContext(contextName);
		
		LocalCacheUtil.put(cacheKey, h2oContext);
		return h2oContext;
	}
	
	/**
	 * TODO add cache
	 * @param classType
	 * @return
	 */
	public static EntityManager getEntityManager(Class<? extends H2OEntity> classType){
		String contextName = getTargetContextName(classType);
		H2OContext h2oContext = SystemContext.getInstance().getContext(contextName);
		return h2oContext.getEntityManager();
	}
	
	/**
	 * get all fields including parent class
	 * @param targetClass
	 * @param allFields
	 */
	public static void resolveAllFields(Class<?> targetClass,List<Field> allFields){
		Field[] fields = targetClass.getDeclaredFields();
		for(Field field:fields){
			field.setAccessible(true);
			allFields.add(field);
		}
		Class<?> parentClass = targetClass.getSuperclass();
		if(parentClass!=Object.class){
			resolveAllFields(parentClass,allFields);
		}
	}
	
	
	public static String getColumnsSql(BindObject bindObject,SQLDialet dialet){
		StringBuilder sb = new StringBuilder();
		
		if(bindObject.getIdColumn()!=null){
			appendColumnSql(bindObject.getIdColumn(),dialet,sb,true);
		}
		
		Map<String,BindColumn> columns = bindObject.getColumns();
		for(BindColumn tmp:columns.values()){
			appendColumnSql(tmp,dialet,sb,false);
		}
		if(bindObject.getVersionColumn()!=null){
			appendColumnSql(bindObject.getVersionColumn(),dialet,sb,false);
		}
		
		String result = sb.toString();
		result = result.substring(0, result.length()-1);
		
		return result;
	}
	
	private static void appendColumnSql(BindColumn bindColumn,SQLDialet dialet,StringBuilder sb, boolean isPrimary){
		sb.append(bindColumn.getName());
		sb.append(" ");
		if(bindColumn.getField().isAnnotationPresent(Text.class)){
			sb.append(dialet.getSqlStrForTextField());
		}
		else{
			sb.append(SQLDialet.getSqlStrByFieldType(dialet,bindColumn.getBindType()));
			if(bindColumn.getLength()!=0){
				sb.append("(");
				sb.append(bindColumn.getLength());
				sb.append(")");
			}
		}
		
		if(isPrimary){
			sb.append(" PRIMARY KEY ");
		}
		sb.append(",");
	}
	
	/**
	 *  Resolve result set to object list
	 * @param rs
	 * @param resultClass
	 * @return
	 * @throws Exception
	 */
	public static <T> List<T> resolveResultSet(ResultSet rs,Class<T> resultClass,H2OContext h2oContext) throws Exception{
		if(resultClass==String.class||resultClass==Long.class||resultClass==Integer.class){
			return resolveResultSetForBasicType(rs,resultClass);
		}
		List<T> result = new ArrayList<T>();
		HashMap<String,BindColumn> metaMap = getClassMapping(resultClass,h2oContext); 
        try {  
        	T tmpObject = null;
        	Field field = null;
        	BindColumn bindColumn = null;
        	Object tmpValue = null;
        	while(rs.next()){
        		tmpObject = resultClass.newInstance();
        		result.add(tmpObject);
        		
        		for(Entry<String,BindColumn> entry : metaMap.entrySet()){
        			bindColumn = entry.getValue();
        			field = bindColumn.getField();
        			field.setAccessible(true);
        			try{
        				rs.findColumn(entry.getKey());
        			}
        			catch(Exception e){
        				continue;
        			}
        			
        			//for date handling
        			if(bindColumn.getBindType() == java.util.Date.class){
        				java.sql.Timestamp sqlDate = rs.getTimestamp(entry.getKey());
        				//java.sql.Date sqlDate = rs.getDate(entry.getKey());
        				if(sqlDate!=null){
        					java.util.Date utilDate = new java.util.Date(sqlDate.getTime());
        					tmpValue = utilDate;
        				}
        				else{
        					tmpValue = null;
        				}
        				field.set(tmpObject, tmpValue);
        				continue;
        			}
        			
        			tmpValue = rs.getObject(entry.getKey());
        			
        			if(tmpValue==null){
        				field.set(tmpObject, null);
        				continue;
        			}
        			//for db special handle
        			if(tmpValue.getClass()!=bindColumn.getBindType()){
        				
        				if(tmpValue.getClass() == BigDecimal.class){
            				BigDecimal x = (BigDecimal)tmpValue;
            				tmpValue = x.longValue();
            				
            				if(bindColumn.getBindType() == Integer.class){
            					tmpValue = Integer.parseInt(String.valueOf(tmpValue));
            				}
            			}
        				else if(tmpValue.getClass() == Integer.class){
        					Integer x = (Integer)tmpValue;
            				
        					if(bindColumn.getBindType() == Long.class){
        						tmpValue = new Long(x.intValue());
        					}
        				}
        			}
        			field.set(tmpObject, tmpValue);
        		}
    		}    
        } catch (Exception e) {  
           throw e;
        } 
        
        return result;
	}
	
	
	private static <T> List<T> resolveResultSetForBasicType(ResultSet rs, Class<T> resultClass) throws Exception {
		List<T> result = new ArrayList<T>();
 
        try {  
        	while(rs.next()){
        		Object tmpValue = null;
        		if(resultClass==String.class){
        			tmpValue = rs.getString(1);
        		}
        		else if(resultClass==Long.class){
        			tmpValue = rs.getLong(1);
        		}
        		else if(resultClass==Integer.class){
        			tmpValue = rs.getInt(1);
        		}
    			result.add((T) tmpValue);   
    		}    
        } catch (Exception e) {  
           throw e;
        } 
        
        return result;
	}

	/**
	 * Return column name and BindObject mapping
	 * @param resultClass
	 * @return
	 */
	private static HashMap<String,BindColumn> getClassMapping(Class<?> resultClass,H2OContext h2oContext){
		HashMap<String,BindColumn> map =  new HashMap<String,BindColumn>();
		BindObject bindObject = h2oContext.getEntityManager().getEntityToTableCache().get(resultClass);
		if(bindObject==null){
			bindObject = new BindObject(resultClass);
			h2oContext.getEntityManager().getEntityToTableCache().put(resultClass, bindObject);
		}
		Map<String, BindColumn> bindColumnsMap = bindObject.getColumns();
		
		BindColumn tmpColumn = bindObject.getIdColumn();
		if(tmpColumn!=null){
			map.put(tmpColumn.getName(), tmpColumn);
		}
		tmpColumn = bindObject.getVersionColumn();
		if(tmpColumn!=null){
			map.put(tmpColumn.getName(), tmpColumn);
		}
		
		for(BindColumn tmp: bindColumnsMap.values()){
			map.put(tmp.getName(), tmp);
		}
		return map;
	}
	
	public static String instanceToString(Object obj,Boolean complex){
		if(obj ==null) return "null";
		List<Field> allFields = new ArrayList<Field>();
		StringBuilder sb = new StringBuilder("");
		if(complex){
			sb.append(obj.getClass().getName());
			sb.append("{");
			sb.append("\r\n");
			resolveAllFields(obj.getClass(),allFields);
			Field field = null;
			String space1 = "         ";
			String space2 = " ";
			int length = allFields.size();
			for(int i = 0 ; i<length ;i++){
				try {
					field = allFields.get(i);
					field.setAccessible(true);
					Object val = field.get(obj);
					sb.append(space1);
					sb.append(field.getType().getSimpleName());
					sb.append(space2);
					sb.append(field.getName());
					sb.append(":");
					sb.append(val);
					if(i==length-1)break;
					sb.append(",");
					sb.append("\r\n");
				} catch (Exception e) {
					e.printStackTrace();
				} 
				
			}
			sb.append("\r\n");
			sb.append("}");
		}
		else{
			sb.append(obj.getClass().getSimpleName());
			sb.append(":");
			resolveAllFields(obj.getClass(),allFields);
			Field field = null;
			int length = allFields.size();
			for(int i = 0 ; i<length ;i++){
				try {
					field = allFields.get(i);
					field.setAccessible(true);
					Object val = field.get(obj);
					sb.append(field.getName());
					sb.append(":");
					sb.append(val);
					if(i==length-1)break;
					sb.append(",");
				} catch (Exception e) {
					e.printStackTrace();
				} 
				
			}
		}
		
		
		return sb.toString();//ToStringBuilder.reflectionToString(obj, ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
	
	/**
	 * Copy value from src object to target obj  begin
	 * @param src
	 * @param target
	 */
	public static void copyProperties(Object src, Object target) {
		Class<?> srcClass = src.getClass();
		HashMap<String,Field> srcFieldMap = resolveClassFieldMapWithCache(srcClass);
		
		Class<?> targetClass = target.getClass();
		HashMap<String,Field> targetFieldMap = resolveClassFieldMapWithCache(targetClass);
		
		for(Entry<String,Field> entry : srcFieldMap.entrySet()){
			String key = entry.getKey();
			Field srcField = entry.getValue();
			Field targetField = targetFieldMap.get(key);
			if(targetField==null) continue;
			
			try{
				if(targetField.getType()!=srcField.getType()){
					if(targetField.getType()==String.class && srcField.getType()==Long.class){
						Long tmp = (Long) srcField.get(src);
						if(tmp!=null)
							targetField.set(target,String.valueOf(tmp));
					}
					else if(targetField.getType()==Long.class && srcField.getType()==String.class){
						String tmp = (String) srcField.get(src);
						targetField.set(target,Long.valueOf(tmp));
					}
					else if(targetField.getType()==String.class && srcField.getType()==Boolean.class){
						Boolean tmp = (Boolean) srcField.get(src);
						targetField.set(target,tmp?"1":"0");
					}
					else if(targetField.getType()==Boolean.class && srcField.getType()==String.class){
						String tmp = (String) srcField.get(src);
						targetField.set(target,"1".equals(tmp)?true:false);
					}
				}
				else{
					targetField.set(target, srcField.get(src));
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private static ConcurrentHashMap<Class<?>,HashMap<String, Field>> classTypeCache = new ConcurrentHashMap<Class<?>,HashMap<String, Field>>();
	
	private static HashMap<String, Field> resolveClassFieldMapWithCache(Class<?> classType){
		HashMap<String, Field> cachedMap = classTypeCache.get(classType);
		if(cachedMap!=null) return cachedMap;
		
		HashMap<String, Field> fieldMap = resolveClassFieldMap(classType);
		classTypeCache.put(classType, fieldMap);
		return fieldMap;
	}
	
	private static HashMap<String, Field> resolveClassFieldMap(Class<?> classType){
		if(Object.class == classType ) return null;
		HashMap<String, Field> fieldMap = new HashMap<String, Field>();
		Field[] fields = classType.getDeclaredFields();
		for(Field field:fields){
			if(Modifier.isStatic(field.getModifiers()))
				continue;
			field.setAccessible(true);
			fieldMap.put(field.getName(), field);
		}
		
		Class<?> superClass = classType.getSuperclass();
		HashMap<String, Field> superFieldMap = resolveClassFieldMap(superClass);
		if(superFieldMap!=null){
			superFieldMap.putAll(fieldMap);
			return superFieldMap;
		}
		else{
			return fieldMap;
		}
	}
	
	public static <T,X> List<T> copyListProperties(List<X> obj,Class<T> c){
		if(obj==null) return null;
		T target = null;
		List<T> result = new ArrayList<T>();
		for(Object tmp:obj){
			try {
				target = c.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			} 
			copyProperties(tmp, target);
			result.add(target);
		}
		return result;
	
	}
	/**
	 * Copy value from src object to target obj  end*/
	
	
	public static String toUpcaseOf1stChar(String source){
		if(source==null||source.length()==0)return source;
		return source.substring(0, 1).toUpperCase() + source.substring(1);
	}
	
	/**
	 * close stream quietly
	 * @param closeable
	 */
	public static void closeQuietly(Closeable closeable){
		try {
			if(closeable!=null)closeable.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	public static void closeQuietly(AutoCloseable closeable){
		try {
			if(closeable!=null)closeable.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	/**
	 * copy file
	 * @param srcFile
	 * @param targetFile
	 * @throws Exception
	 */
	public static void copyFile(String srcFile,String targetFile) throws Exception{
		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel fcin = null;
		FileChannel fcout = null;
		try {
			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(targetFile);

			fcin = fis.getChannel();
			fcout = fos.getChannel();

			ByteBuffer buffer = ByteBuffer.allocate(1024);

			while (true) {
				buffer.clear();
				int r = fcin.read(buffer);
				if (r == -1) {
					break;
				}
				buffer.flip();
				fcout.write(buffer);
			}
		}
		catch(Exception e){
			throw e;
		}
		finally{
			closeQuietly(fcout);
			closeQuietly(fcin);
			closeQuietly(fos);
			closeQuietly(fis);
		}
	}
	
	public static void copyFile(File srcFile,File targetFile) throws Exception{
		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel fcin = null;
		FileChannel fcout = null;
		try {
			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(targetFile);

			fcin = fis.getChannel();
			fcout = fos.getChannel();

			ByteBuffer buffer = ByteBuffer.allocate(1024);

			while (true) {
				buffer.clear();
				int r = fcin.read(buffer);
				if (r == -1) {
					break;
				}
				buffer.flip();
				fcout.write(buffer);
			}
		}
		catch(Exception e){
			throw e;
		}
		finally{
			closeQuietly(fcout);
			closeQuietly(fcin);
			closeQuietly(fos);
			closeQuietly(fis);
		}
	}

	public static void copyFileToDirectory(File srcFile, File outputFolder) throws Exception {
		File outputFile = new File(outputFolder.getAbsolutePath()+Constants.SYSTEM_SEPERATOR+srcFile.getName());
		copyFile(srcFile,outputFile);
		
	}
	
	
	/**
	 * zip folder begin
	 */
	/**
	 * 
	 * @param folderPath return the actual path of the generated zip file
	 * @return
	 * @throws Exception
	 */
	public static String zipFolder(String folderPath) throws Exception{
    	File folder = new File(folderPath);
    	if(!folder.exists()){
    		throw new FileNotFoundException(folderPath);
    	}
    	String zipPath = folderPath+".zip";
    	File zipFile = new File(zipPath);
    	if(zipFile.exists()){
    		zipFile.delete();
    	}
    	
    	String folderName = folder.getName();
    	ZipOutputStream out = null;
    	try{
    		out=new ZipOutputStream(new FileOutputStream(zipPath));
    		byte[] buf=new byte[1024];
        	zipFolder(folder,out,buf,folderName);
    	}
    	finally{
    		CommonUtil.closeQuietly(out);
    	}
    	return zipPath;
    }
    
    private static void zipFolder(File folder,ZipOutputStream out ,byte[] buf,String baseDir) throws IOException{
    	for(File file:folder.listFiles()){
    		if(file.isDirectory()){
    			zipFolder(file,out,buf,baseDir+Constants.SYSTEM_SEPERATOR+file.getName());
    		}
    		else{
    			zipFile(file,out,buf,baseDir);
    		}
    	}
    }
    
    private static void zipFile(File file,ZipOutputStream out ,byte[] buf,String baseDir) throws IOException{
    	FileInputStream in = null;
    	try{
    		in=new FileInputStream(file);
    		out.putNextEntry(new ZipEntry(baseDir+Constants.SYSTEM_SEPERATOR+file.getName()));
    		int len;
    		while ((len = in.read(buf)) > 0) {
    			out.write(buf, 0, len);
    		}
    	}
    	finally{
    		out.closeEntry();
    		CommonUtil.closeQuietly(in);
    	}	
    }
    /**
	 * zip folder end
	 */
    
    public static void deleteFolderOrFile(File file){
    	if(file==null||!file.exists())return;
    	if(file.isFile()){
    		file.delete();
    		return;
    	}
    	else{
    		File[] files = file.listFiles();
    		for(File tmp:files){
    			deleteFolderOrFile(tmp);
    		}
    		file.delete();
    	}
    }
    
    public static String getRequestIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String forwarded = request.getHeader("X-Forwarded-For");
        String realIp = request.getHeader("X-Real-IP");

        String ip = null;
        if (realIp == null) {
            if (forwarded == null) {
                ip = remoteAddr;
            } else {
                ip = remoteAddr + "/" + forwarded.split(",")[0];
            }
        } else {
            if (realIp.equals(forwarded)) {
                ip = realIp;
            } else {
                if(forwarded != null){
                    forwarded = forwarded.split(",")[0];
                }
                ip = realIp + "/" + forwarded;
            }
        }
        return ip;
    }

	public static void downloadFile(HttpServletResponse response, String downLoadPath ,String fileName) {
		if(response instanceof RegionHttpResponse){
			((RegionHttpResponse) response).handleFile(downLoadPath,false,fileName);
			return;
		}
		else{
			if(downLoadPath==null){
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			File file = new File(downLoadPath);
			
			if (!file.exists()) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			response.reset();
			if(fileName==null)fileName = file.getName();
			String contentType = new MimetypesFileTypeMap().getContentType(file);
			response.setContentType(contentType);
			try {
				response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName,"UTF-8"));
			} catch (UnsupportedEncodingException e) {
				try {
					response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName,"UTF-16"));
				} catch (UnsupportedEncodingException e1) {
					logger.error(e1);
				}
			}

			FileInputStream fileInputStream = null;
			BufferedInputStream bufferedInputStream = null;
			try {
				fileInputStream = new FileInputStream(file);
				bufferedInputStream = new BufferedInputStream(fileInputStream);
				int size = 0;
				byte[] b = new byte[4096];
				while ((size = bufferedInputStream.read(b)) != -1) {
					response.getOutputStream().write(b, 0, size);
				}
			} catch (Exception e) {
				logger.warn(e);
			} finally {
				try {
					response.getOutputStream().flush();
				} catch (IOException e) {
					logger.error(e);
				}
				CommonUtil.closeQuietly(bufferedInputStream);
				CommonUtil.closeQuietly(fileInputStream);
			}
		}
		
	}
	
	public static void viewFile(HttpServletResponse response, String downLoadPath ,String fileName) {
		if(response instanceof RegionHttpResponse){
			((RegionHttpResponse) response).handleFile(downLoadPath,true,fileName);
			return;
		}
		else{
			if(downLoadPath==null){
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			File file = new File(downLoadPath);
			
			if (!file.exists()) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			response.reset();
			if(fileName==null)fileName = file.getName();
			String contentType = new MimetypesFileTypeMap().getContentType(file);
			response.setContentType(contentType);
			try {
				response.addHeader("Content-Disposition", "inline; filename=" + URLEncoder.encode(fileName,"UTF-8"));
			} catch (UnsupportedEncodingException e) {
				try {
					response.addHeader("Content-Disposition", "inline; filename=" + URLEncoder.encode(fileName,"UTF-16"));
				} catch (UnsupportedEncodingException e1) {
					logger.error(e1);
				}
			}

			FileInputStream fileInputStream = null;
			BufferedInputStream bufferedInputStream = null;
			try {
				fileInputStream = new FileInputStream(file);
				bufferedInputStream = new BufferedInputStream(fileInputStream);
				int size = 0;
				byte[] b = new byte[4096];
				while ((size = bufferedInputStream.read(b)) != -1) {
					response.getOutputStream().write(b, 0, size);
				}
			} catch (Exception e) {
				logger.warn(e);
			} finally {
				try {
					response.getOutputStream().flush();
				} catch (IOException e) {
					logger.error(e);
				}
				CommonUtil.closeQuietly(bufferedInputStream);
				CommonUtil.closeQuietly(fileInputStream);
			}
		}
		
	}

	public static List<FileItem> resolveUploadFileInfo(Map<String, String> reqMap, HttpServletRequest request) throws Exception {
		List<FileItem> files = new ArrayList<FileItem>();
		if(request instanceof RegionHttpRequest){//for standalone app
			RegionHttpRequest regionHttpRequest = (RegionHttpRequest) request;
			UploadResolver uploadResolver = new UploadResolver();
			//System.out.println("resolveUploadFileInfo");
			if(regionHttpRequest.getUploadMsg()!=null) {
				//System.out.println(regionHttpRequest.getUploadMsg());
				String tmpFilePath = Constants.FILE_SERVER_ROOT+Constants.SYSTEM_SEPERATOR+"tmp"+Constants.SYSTEM_SEPERATOR+regionHttpRequest.getUploadMsg().getFileName();
				List<RegionFileItem> fileItemLs = uploadResolver.resolveUpload(tmpFilePath);
				
				for(RegionFileItem item:fileItemLs){
					if(!item.isFile()){
						reqMap.put(item.getFieldName(), item.getString());
					}
					else{
						files.add(item);
					}
				}
			}
		}
        else{//for servlet app
        	DiskFileItemFactory factory = new DiskFileItemFactory();  
	        File cacheFolder = new File(Constants.FILE_SERVER_ROOT+Constants.SYSTEM_SEPERATOR+"tmp");
	        factory.setRepository(cacheFolder);
	        
	        ServletFileUpload upload = new ServletFileUpload(factory);
			List<FileItem> items = upload.parseRequest(request);
			Iterator<FileItem> iter = items.iterator();
			
			while (iter.hasNext()) {
			    FileItem item = iter.next();
			    if (item.isFormField()) {
			    	reqMap.put(item.getFieldName(), item.getString());
			    } else {
			    	files.add(item);
			    }
			}
        }
		return files;
	}
	
	
	//error info
	public static String constructErrorStr(Object... objs){
		StringBuilder buf = new StringBuilder();
		buf.append(lineNumber());
		for (Object obj : objs) {
			if(obj instanceof Throwable){
				buf.append(constructStackTrace((Throwable) obj));
			}
			else if(obj instanceof Object[]){
				buf.append("[");
				Object[] array = (Object[])obj;
				for(int i = 0 ; i <array.length ; i++){
					buf.append(array[i]);
					if(i!=(array.length-1))buf.append(",");
				}
				buf.append("]");
			}
			else{
				buf.append(obj);
			}
		}
		return buf.toString();
	}
	
	public static String lineNumber() {
		StackTraceElement threadTrackArray[] = Thread.currentThread().getStackTrace();
		if (threadTrackArray.length > 3) {
			return ":" + Integer.toString(threadTrackArray[3].getLineNumber());
		}
		return "";
	}
	
	public static String constructStackTrace(Throwable t) {
		try {
			return getStackTrace(t);
		} catch (Exception e) {
			return e.getMessage();
		}
	}
	
	public static String getStackTrace(Throwable aThrowable) throws Exception {
		Writer stack = null;
		PrintWriter printWriter = null;
		try{
			stack = new StringWriter();
			printWriter = new PrintWriter(stack);
			aThrowable.printStackTrace(printWriter);
			return stack.toString();
		}
		catch(Exception e){
			throw e;
			
		}
		finally{
			CommonUtil.closeQuietly(printWriter);
			CommonUtil.closeQuietly(stack);
		}
	}
	
	public static byte[] intToByte4(int i) {  
        byte[] targets = new byte[4];  
        targets[3] = (byte) (i & 0xFF);  
        targets[2] = (byte) (i >> 8 & 0xFF);  
        targets[1] = (byte) (i >> 16 & 0xFF);  
        targets[0] = (byte) (i >> 24 & 0xFF);  
        return targets;  
    }
	
	public static int byte4ToInt(byte[] bytes, int off) {
		int b0 = bytes[off] & 0xFF;
		int b1 = bytes[off + 1] & 0xFF;
		int b2 = bytes[off + 2] & 0xFF;
		int b3 = bytes[off + 3] & 0xFF;
		return (b0 << 24) | (b1 << 16) | (b2 << 8) | b3;
	}
	
	public static String getCookie(HttpServletRequest request,String cookieKey){
		if(request instanceof RegionHttpRequest) {
			return ((RegionHttpRequest) request).getCookiesMap().get(cookieKey);
		}
		else {
			Cookie[] cookies = request.getCookies();
			String loginToken = null;
			if(cookies!=null){
				for(Cookie cookie:cookies){
					if(cookie.getName().equals(cookieKey)){
						loginToken = cookie.getValue();
						break;
					}
				}
			}
			return loginToken;
		}
	}
	
	
	//private static final String injStr = "'|and|exec|insert|select|delete|update|count|*|%|chr|mid|master|truncate|char|declare|;|or|-|+|,";
	private static final String injStr = "'|and|exec|insert|select|delete|count|*|%|chr|mid|master|truncate|char|declare|;|or|-|+|,";
	private static final String[] injStra = injStr.split("\\|");
	/**
	 * 判断sql注入
	 * @param str
	 * @return
	 * @throws Exception 
	 */
	public static void checkSQLInject(String sql) throws Exception {
		for (int i = 0; i < injStra.length; i++) {
			if (sql.indexOf(injStra[i]) !=-1) {
				throw new Exception("sql is invalid");
			}
		}
	}
	

	
	private static String objectToString(Object object) {
		StringBuilder b = new StringBuilder();
		
		if(object != null) {
			if(object instanceof Map) {
				Map<String,Object> tmp = (Map<String, Object>) object;
				Iterator<Entry<String,Object>> iterator = tmp.entrySet().iterator();
				b.append("[");
				while(iterator.hasNext()) {
					Entry<String,Object> entry = iterator.next();
					b.append(entry.getKey());
					b.append(":");
					b.append(objectToString(entry.getValue()));
					b.append(",");
				}
				b.append("]");
			}
			else b.append(object);
		}
		return b.toString();
	}
	
	//在content里找到以startTag开头 endTag结尾的块.如果找到则返回length为3的数组,否则返回length为1的数组
	public static String[] findFirstTagContent(String content,String startTag,String endTag) {
		if(isEmpty(content)) {
			return new String[] {""};
		}
		int imgStartIndex = content.indexOf(startTag);
		if(imgStartIndex!=-1){//find start of img tag 
			String part1 = content.substring(0,imgStartIndex);
			String part2 = content.substring(imgStartIndex+startTag.length());
		
			int endTagIndex = part2.indexOf(endTag);
			if(endTagIndex!=-1){
				String tagContent = startTag+part2.substring(0,endTagIndex+endTag.length());
				String part3 = part2.substring(endTagIndex+endTag.length());
				
				return new String[] {part1,tagContent,part3};
			}
		}
		return new String[] {content};
	}
	
	/**
	 * 获取占位符的名字LIST
	 * @param content
	 * @param startTag
	 * @param endTag
	 * @return
	 */
	public static List<String> getOccupationParanames(String content,String startTag,String endTag) {
		List<String> result = new ArrayList<String>();
		if(isEmpty(content)) {
			return result;
		}
		
		int imgStartIndex = content.indexOf(startTag);
		while(imgStartIndex!=-1){//find start of img tag
			String part2 = content.substring(imgStartIndex+startTag.length());
		
			int endTagIndex = part2.indexOf(endTag);
			if(endTagIndex!=-1){
				String tagContent = part2.substring(0,endTagIndex);
				result.add(tagContent);
				content = part2.substring(endTagIndex+endTag.length());
				imgStartIndex = content.indexOf(startTag);
			}
		}
		return result;
	}
	
	private static final char[] CHARACTERS = "123456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
	//把Long编码成字符串
	public static String encodeLong(long val) {
		StringBuilder result = new StringBuilder();
		
		int x = CHARACTERS.length;//x 进制
		long quotient = val/x;
		long remainder = val%x;//quotient
		
		result.append(CHARACTERS[(int) remainder]);
		if(quotient<x) {
			if(quotient!=0)
				result.append(CHARACTERS[(int) quotient]);
			result = result.reverse();
			return result.toString();
		}
		
		while(quotient>=x) {//还可以除
			val = quotient;
			quotient = val/x;
			remainder = val%x;
			result.append(CHARACTERS[(int) remainder]);
		}
		
		if(quotient!=0)
			result.append(CHARACTERS[(int) quotient]);
		result = result.reverse();
		return result.toString();
	}
	
	public static List<String> convertStringArrayToList(String[] array) {
		if(array==null)return null;
		List<String> result = new ArrayList<String>();
		for(String tmp:array) {
			if(CommonUtil.isEmpty(tmp))continue;
			result.add(tmp);
		}
		return result;
	}
	
//	CommonUtil.replace("你的用户名是${user},密码是${password}。系统版本${system-${版本}}",params,"${","}",true));
//    (CommonUtil.replace("表达对一个人无比的崇拜怎么表述最好？答：“愿闻其${详}”",params,"${","}",false)
	public static String replace(String source,Map<String, Object> parameter,String prefix, String suffix,boolean enableSubstitutionInVariables){
        StrSubstitutor strSubstitutor = new StrSubstitutor(parameter,prefix, suffix);
        strSubstitutor.setEnableSubstitutionInVariables(enableSubstitutionInVariables);
        return strSubstitutor.replace(source);
    }
	
	
	public static String wrapText(String orignalText,Map<String, Object> params) {
		return CommonUtil.replace(orignalText,params,Constants.OCCUP_START,Constants.OCCUP_END,false);
	}
	
	//把参数列表转化为cache key
	public static String buildCacheKey(Object... args) {
		Assert.notNull(args, "args is empty");
		final StringBuilder b = new StringBuilder();
		for(Object object:args) {
			b.append(Constants.MINUS);
			b.append(objectToString(object));
		}
		return b.toString();
	}
	
	/**
	 * 取clz类 方法method的参数map
	 * @param clz
	 * @param method
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static MethodMeta getMethodMeta(Class<?> clz, Method method) throws Exception{
		String methodName = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		
		int hashCode = clz.getName().hashCode()*31 +methodName.hashCode();
		if(parameterTypes!=null) {
			for(Class<?> tmp:parameterTypes) {
				hashCode = hashCode*31+tmp.getName().hashCode();
			}
		}
		String cacheKey = String.valueOf(hashCode);
		Object cachedObj = LocalCacheUtil.get(cacheKey);
		if(cachedObj!=null) {
			return (MethodMeta) cachedObj;
		}
		
		Map<String,Integer> paraMaps = new LinkedHashMap<String,Integer>();
		ClassPool pool = ClassPool.getDefault();
		
		String proxyClassName = clz.getName();
		int cglibTag = proxyClassName.indexOf("$$");
		String origClassName = null;
		if(cglibTag!=-1) {
			origClassName = proxyClassName.substring(0,proxyClassName.indexOf("$$"));
		}
		else {
			origClassName = proxyClassName;
		}

		CtClass ctClass = pool.get(origClassName);
		CtMethod cm = ctClass.getDeclaredMethod(methodName);
		MethodInfo methodInfo = cm.getMethodInfo();
		CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
		LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
		String[] paramNames = new String[cm.getParameterTypes().length];
		int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
		
		for (int j = 0; j < paramNames.length; j++)
			paramNames[j] = attr.variableName(j + pos);
		
		for (int i = 0; i < paramNames.length; i++) {
				paraMaps.put(paramNames[i], i);
		}
			
		ReturnClassType returnClassType = null;
		Type[] argumentsOfReturnClass = null;
			
		Type type = method.getGenericReturnType();
		if (type instanceof ParameterizedType) {// 返回值带参数或者泛型的处理
			ParameterizedType parameterizedType = (ParameterizedType) type;
			if(parameterizedType.getRawType()==List.class) {
				returnClassType = ReturnClassType.LIST;
				argumentsOfReturnClass = parameterizedType.getActualTypeArguments();
			}
			else {
				returnClassType = ReturnClassType.OTHERS;
			}
		}
		else {
			returnClassType = ReturnClassType.POJO;
		}
			
		MethodMeta methodMeta = new MethodMeta(paraMaps,returnClassType,argumentsOfReturnClass,type,cacheKey,parameterTypes,method.getParameterAnnotations());
		LocalCacheUtil.put(cacheKey, methodMeta);
		return methodMeta;
	}
	
	
	public static MethodMeta getInterfaceMeta(Class<?> interfaceClz, Method method) throws Exception{
		String methodName = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		
		int hashCode = interfaceClz.getName().hashCode()*31 +methodName.hashCode();
		if(parameterTypes!=null) {
			for(Class<?> tmp:parameterTypes) {
				hashCode = hashCode*31+tmp.getName().hashCode();
			}
		}
		String cacheKey = String.valueOf(hashCode);
		Object cachedObj = LocalCacheUtil.get(cacheKey);
		if(cachedObj!=null) {
			return (MethodMeta) cachedObj;
		}
		
		Map<String,Integer> paraMaps = new LinkedHashMap<String,Integer>();

		ReturnClassType returnClassType = null;
		Type[] argumentsOfReturnClass = null;
			
		Type type = method.getGenericReturnType();
		if (type instanceof ParameterizedType) {// 返回值带参数或者泛型的处理
			ParameterizedType parameterizedType = (ParameterizedType) type;
			if(parameterizedType.getRawType()==List.class) {
				returnClassType = ReturnClassType.LIST;
				argumentsOfReturnClass = parameterizedType.getActualTypeArguments();
			}
			else {
				returnClassType = ReturnClassType.OTHERS;
			}
		}
		else {
			returnClassType = ReturnClassType.POJO;
		}
			
		MethodMeta methodMeta = new MethodMeta(paraMaps,returnClassType,argumentsOfReturnClass,type,cacheKey,parameterTypes,method.getParameterAnnotations());
		LocalCacheUtil.put(cacheKey, methodMeta);
		return methodMeta;
	}
	
	@SuppressWarnings("unchecked")
	public static Method[] getDeclaredMethods(Class<?> clz) throws Exception{
		String cacheKey = LocalCacheUtil.GET_DECLARED_METHODS+clz.getName();
		Object cachedObj = LocalCacheUtil.get(cacheKey);
		if(cachedObj==null) {
			cachedObj = clz.getDeclaredMethods();
			LocalCacheUtil.put(cacheKey, cachedObj);
		}
		return (Method[]) cachedObj;
	}

	//获取对象的嵌套属性
//	String fieldExpression = "entity.question.content";
//	getObjectProperty(questionRepo,fieldExpression)
	public static Object getEmbededAttributeValue(Object object, String[] fieldArray) throws Exception {
		Field field = null;
		for(int i = 1;i<fieldArray.length;i++) {
			field = getField(object.getClass(),fieldArray[i]);
			if(field==null)return null;
			
			field.setAccessible(true);
			object = field.get(object);
			if(object==null)return null;
		}
		
		return object;
	}
	
	
	public static Field getField(Class classType,String fieldName) {
		if(classType==Object.class)return null;
		
		String cacheKey  = LocalCacheUtil.GET_FIELD+classType.getName()+fieldName;
		Field field = (Field) LocalCacheUtil.get(cacheKey);
		if(field!=null)return field;
		
		while(field == null){
			try {
				field = classType.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				classType = classType.getSuperclass();
				if(classType==null||classType==Object.class) {
					break;
				}
			}
		}
		
		if(field!=null)LocalCacheUtil.put(cacheKey, field);
		return field;
	}

	public static String getAllFieldsAsString(Class<? extends Object> clazz) {		
		String cacheKey  = LocalCacheUtil.GET_FIELDS_AS_STR+clazz.getName();
		String cachedStr = (String) LocalCacheUtil.get(cacheKey);
		if(cachedStr!=null)return cachedStr;
		
		TreeSet<String> fieldsNameSet = new TreeSet<String>();
		Field[] fields = clazz.getDeclaredFields();
		for(Field tmp:fields) {
			fieldsNameSet.add(tmp.getName());
		}
		
		clazz = clazz.getSuperclass();
		while(clazz != null&&clazz!=Object.class){
			fields = clazz.getDeclaredFields();
			for(Field tmp:fields) {
				fieldsNameSet.add(tmp.getName());
			}
			clazz = clazz.getSuperclass();
		}
		
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for(String fieldName:fieldsNameSet) {
			if(i!=0)sb.append(Constants.UNDER_LINE);
			sb.append(Constants.OCCUP_START);
			sb.append(fieldName);
			sb.append(Constants.OCCUP_END);
			i++;
		}
		
		String result = sb.toString();
		if(result!="")LocalCacheUtil.put(cacheKey, result);
		return result;
	}
	
	
	@SuppressWarnings("unchecked")
	public static Map<String,Field> getAllFields(Class<? extends Object> clazz) {		
		String cacheKey  = LocalCacheUtil.GET_ALL_FIELDS+clazz.getName();
		Object cachedObject =  LocalCacheUtil.get(cacheKey);
		if(cachedObject!=null)return (Map<String,Field>) cachedObject;
		
		Map<String,Field> fieldsMap = new HashMap<String,Field>();
		Field[] fields = clazz.getDeclaredFields();
		for(Field tmp:fields) {
			fieldsMap.put(tmp.getName(),tmp);
		}
		
		clazz = clazz.getSuperclass();
		while(clazz != null&&clazz!=Object.class){
			fields = clazz.getDeclaredFields();
			for(Field tmp:fields) {
				fieldsMap.put(tmp.getName(),tmp);
			}
			clazz = clazz.getSuperclass();
		}
		
		if(fieldsMap.size()!=0)LocalCacheUtil.put(cacheKey, fieldsMap);
		return fieldsMap;
	}
	
	
	public static Date now() {
		return Calendar.getInstance(TimeZone.getTimeZone(Constants.GMT8)).getTime();
	}
	
	public static Date toDate(Long milliseconds) {
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.GMT8));
		calendar.setTimeInMillis(milliseconds);
		return calendar.getTime();
	}
	 

	/**
	 * 把一个文件分割成两个文件
	 * @param filePath
	 */
//	public static void splitFile(String filePath) {
//		FileInputStream is = null;
//		FileOutputStream os1 = null;
//		FileOutputStream os2 = null;
//		try {
//			is = new FileInputStream(filePath);
//			int file1Size = is.available() / 2;
//			int buffersize = 512;
//			file1Size = (file1Size / buffersize) * buffersize;
//			byte buffer[] = new byte[buffersize];
//			int i = 0;
//			os1 = new FileOutputStream(filePath + "1");// file 1
//			os2 = new FileOutputStream(filePath + "2");// file 2
//			while ((is.read(buffer, 0, buffersize) != -1) && (buffersize > 0)) {
//				if ((i < file1Size / buffersize)) {
//					os1.write(buffer);
//				} else {
//					os2.write(buffer);
//				}
//				i++;
//			}
//
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		finally {
//			CommonUtil.closeQuietly(os1);
//			CommonUtil.closeQuietly(os2);
//			CommonUtil.closeQuietly(is);
//		}
//	}
	
	/**
	 * 把多个文件合并成一个文件
	 * @param filePaths
	 * @param targetFolder
	 * @param targetFileName
	 */
	public static void mergeFiles(String[] filePaths, String targetFolder,String targetFileName) {
		File file = null;
		file = new File(targetFolder);
		if(!file.exists()) {
			file.mkdirs();
		}
		else if(!file.isDirectory()) {
			throw new RuntimeException("targetFolder is file");
		}
		File targetFile = new File(file.getAbsolutePath()+Constants.PATH_DELIMETER+targetFileName);
		
		FileOutputStream fos = null;
		FileInputStream fis = null;
		try {
			fos = new FileOutputStream(targetFile);
			for(String filePath:filePaths) {
				file = new File(filePath);			
				fis = new FileInputStream(file);

				int buffersize = 512;
				byte buffer[] = new byte[buffersize];
				
				int available = 0;
				while ((available=fis.read(buffer, 0, buffersize)) != -1) {
					fos.write(buffer, 0, available);
				}
				
				fos.write('\r');
				fos.write('\n');
				fos.flush();
				fis.close();
				fis = null;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			CommonUtil.closeQuietly(fis);
			CommonUtil.closeQuietly(fos);
		}
	}
	
	/***
	  * 压缩Zip
	  * @param data
	  * @return
	  */
	public static byte[] zip(byte[] data) {
		byte[] b = null;
		ByteArrayOutputStream bos = null;
		ZipOutputStream zip = null;
		ZipEntry entry = null;
		try {
			bos = new ByteArrayOutputStream();
			zip = new ZipOutputStream(bos);
			entry = new ZipEntry("zip");
			entry.setSize(data.length);
			zip.putNextEntry(entry);
			zip.write(data);
			b = bos.toByteArray();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			try {
				zip.closeEntry();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				zip.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			closeQuietly(bos);
		}
		return b;
	}
	
	
	public static byte[] gzip(byte[] data) throws UnsupportedEncodingException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = null;
		try {
			gzip = new GZIPOutputStream(out);
			gzip.write(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			closeQuietly(gzip);
		}
		
		return out.toByteArray();
	}
	
	public static byte[] ungzip(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        GZIPInputStream ungzip = null;
        try {
            ungzip = new GZIPInputStream(in);
            byte[] buffer = new byte[256];
            int n;
            while ((n = ungzip.read(buffer)) >= 0) {
                out.write(buffer, 0, n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
        	closeQuietly(ungzip);
        	closeQuietly(in);
        }
        return out.toByteArray();
    }
}
