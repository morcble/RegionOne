package cn.regionsoft.one.assist.field;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

public class FieldsProxyAssist {
	private static Map<Integer,FieldsProxy> fieldProxyClassMap = new ConcurrentHashMap<Integer,FieldsProxy>();
	
	public static FieldsProxy genFieldProxy(Class classType, String fieldName) throws CannotCompileException, NotFoundException, InstantiationException, IllegalAccessException, IOException {

		int hashCode = classType.getName().hashCode()*31+fieldName.hashCode();
		if(hashCode<0)hashCode=-hashCode;
		
		FieldsProxy cachedProxy =  fieldProxyClassMap.get(hashCode);
		if(cachedProxy!=null) {
			return cachedProxy;
		}
		
		synchronized(FieldsProxyAssist.class) {
			cachedProxy = (FieldsProxy) fieldProxyClassMap.get(classType);
			if(cachedProxy!=null) return cachedProxy;
			
			String className = classType.getName();	
			ClassPool pool = ClassPool.getDefault();
	        /**
	         * gen the field proxy
	         */
	        CtClass cc = null;
	        cc = pool.makeClass(className+"_Proxy"+fieldName);
	        
	        cc.addInterface(pool.get("cn.regionsoft.one.assist.field.FieldsProxy"));
	        StringBuilder methodBody = new StringBuilder("public Object getVal(Object srcObj){");
	        
	        methodBody.append(classType.getName() +" targetObj=("+classType.getName()+")srcObj;");
	        
	        String tmpName  = null;
	        if(fieldName.length()>1)
	        	tmpName = fieldName.substring(0, 1).toUpperCase()+fieldName.substring(1);
        	else
        		tmpName = fieldName.toUpperCase();
        	
        	methodBody.append("return targetObj.get"+tmpName+"();");
	        
	        methodBody.append("}");
	        
	        CtMethod m1=CtMethod.make(methodBody.toString(), cc);
	        cc.addMethod(m1);
	        Class<?> realClass = (Class<?>) cc.toClass();

	        cc.detach();
	        
	        FieldsProxy newInstance = (FieldsProxy) realClass.newInstance();
	        fieldProxyClassMap.put(hashCode,newInstance);
	        return newInstance;
		}
		
		
	}
	
}
