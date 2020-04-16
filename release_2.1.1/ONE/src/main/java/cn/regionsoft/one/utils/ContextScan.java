package cn.regionsoft.one.utils;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import cn.regionsoft.one.common.Logger;

//import cn.regionsoft.xframe.data.DBType;

/**
 * find all the class in package
 */
public class ContextScan {
	private static final Logger logger = Logger.getLogger(ContextScan.class);
    public static HashSet<Class<?>> getClassListByPackage(String basePackage) throws Exception {
    	String realPackagePath = basePackage.replaceAll("\\.", "/");
    	HashSet<Class<?>> classList = new HashSet<Class<?>>();
        doScan(realPackagePath,ContextScan.class.getClassLoader(),classList);
        return classList;
    }

    private static void scanByUrl(URL url,String realPackagePath, ClassLoader cl,HashSet<Class<?>> classList) throws Exception{
    	String filePath = ContextScan.getRootPath(url);

        List<String> names = null; 
        if (filePath.endsWith(".jar")) {
            names = readFromJarFile(filePath, realPackagePath);
            for (String name : names) {
                if (name.endsWith(".class")) {
                	Class<?> tmp = null;
                	try {
                		if(name.indexOf("$")==-1)//ignore inner classes
                			tmp = Class.forName(filterClassName(name.replaceAll("/", "\\.")));
                	}
                	catch (ClassNotFoundException e) {
                		logger.error(e);
             		}
                	if(tmp!=null)classList.add(tmp);
                	
                } else {
                	String tmpPath = (realPackagePath + "." + name).replaceAll("\\.", "/");
                	doScan(tmpPath,cl, classList);
                }
            }
        } 
        else{
        	names = readFromDirectory(filePath);
            for (String name : names) {
                if (name.endsWith(".class")) {
                	Class<?> tmp = toFullyQualifiedName(name, realPackagePath);
                	if(tmp!=null)classList.add(tmp);
                } else {
                	String tmpPath = (realPackagePath + "." + name).replaceAll("\\.", "/");
                	doScan(tmpPath,cl, classList);
                }
            }
        }
        
        
    }
    
    public static String filterClassName(String nameInJar){
    	return nameInJar.substring(0,nameInJar.length()-6);
    }
    
    public static String getRootPath(URL url) {
        String fileUrl = url.getFile();
        int pos = fileUrl.indexOf('!');

        if (-1 == pos) {
            return fileUrl;
        }

        return fileUrl.substring(5, pos);
    }
    
    private static void doScan(String realPackagePath, ClassLoader cl,HashSet<Class<?>> classList) throws Exception {
        Enumeration<URL> urlLs = cl.getResources(realPackagePath);
        URL url = null;
        while(urlLs.hasMoreElements()){
        	 url = urlLs.nextElement();
        	 scanByUrl(url,realPackagePath,cl,classList);
        }
    }

    private static Class<?> toFullyQualifiedName(String shortName, String basePackage){
        StringBuilder sb = new StringBuilder(basePackage);
        sb.append('.');
        sb.append(ContextScan.trimExtension(shortName));
        Class<?> result = null;
		try {
			result = Class.forName(sb.toString().replaceAll("/", "\\."));
		} catch (ClassNotFoundException e) {
			logger.error(e);
			return null;
		}
        return result;
    }

    private static List<String> readFromJarFile(String jarPath, String splashedPackageName) throws Exception {
    	JarInputStream jarIn = null;
    	List<String> classList = new ArrayList<String>();
    	try{
			jarIn = new JarInputStream(new FileInputStream(jarPath));
			JarEntry entry = jarIn.getNextJarEntry();
			while (null != entry) {
				String name = entry.getName();
				if (name.startsWith(splashedPackageName) && name.endsWith(".class")) {
					classList.add(name);
				}
				entry = jarIn.getNextJarEntry();
			}
    	}
    	catch(Exception e){
    		throw e;
    	}
    	finally{
    		jarIn.close();
    	}
        return classList;
    }

    private static List<String> readFromDirectory(String path) {
        File file = new File(path);
        String[] names = file.list();
        if (null == names) {
            return null;
        }
        return Arrays.asList(names);
    }


    public static String trimExtension(String name) {
        int pos = name.indexOf('.');
        if (-1 != pos) {
            return name.substring(0, pos);
        }
        return name;
    }


    public static String trimURI(String uri) {
        String trimmed = uri.substring(1);
        int splashIndex = trimmed.indexOf('/');
        return trimmed.substring(splashIndex);
    }
    
   /* public static List<Class<?>> scanPackageByAnnotation(String packagPath,Class<?> annotation) throws Exception{
    	List<Class<?>> result = ContextScan.getClassListByPackage(packagPath);
    	return result;
    }*/
/*
    private static void handleDaoAno(Class<?> tmp) {
		// TODO Auto-generated method stub
		
	}

    private static void handleEntityAno(Class<?> classType){
    	Annotation tmpAno = classType.getAnnotation(Entity.class);
    	if(tmpAno!=null){
    		System.out.println(tmpAno.annotationType().isAnnotationPresent(ChildAno.class));
    	}
    	
    }*/
/*    

    public static void main(String[] args) throws Exception {
    	HashSet<Class<?>> result = ContextScan.getClassListByPackage("com.morcble.persistence.entity");
        for(Class<?> tmp:result){
        	//handleEntityAno(tmp);
        	//handleDaoAno(tmp);
        	
        }
        
        System.out.println(DBType.MYSQL.getValidateQuery());
    }
    */

}