package cn.regionsoft.one.core.logger;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import cn.regionsoft.one.common.Logger;



public class CustomizedClassloaderUtil {
	private static CustomizedClassloader customizedClassLoader = new CustomizedClassloader( new URL[] {},null);
	private static Class logManagerClass = null;
	static{
		ClassLoader tmpLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(customizedClassLoader);
		customizedClassLoader.setSystemClassLoader(CustomizedClassloader.class.getClassLoader());
		try {
			customizedClassLoader.addJar(CustomizedClassloaderUtil.class.getClassLoader().getResource("log4j-api-2.9.1.jar"));
			customizedClassLoader.addJar(CustomizedClassloaderUtil.class.getClassLoader().getResource("log4j-core-2.9.1.jar"));
			customizedClassLoader.addJar(CustomizedClassloaderUtil.class.getClassLoader().getResource("logProxy.jar"));
			
			logManagerClass = customizedClassLoader.loadClass("cn.regionsoft.xframe.common.LoggerProxy");
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			Thread.currentThread().setContextClassLoader(tmpLoader);
			customizedClassLoader = null;
		}
	}
	
	

	public static Object invokeMethod(String methodName,Object... args) throws Exception {
		try {
            Method[] methods =  logManagerClass.getDeclaredMethods();
    		for(Method method:methods){
    			if(method.getName().equals(methodName)){
    				return method.invoke(logManagerClass, args);
    			}
    		}
        } catch (Exception e) {
        	e.printStackTrace();
        }
		
		return null;
	}
	
	/*public static void main(String[] args) throws Exception {
		//TestClassloader.sendMail();
		
	}
	*/
	
	
	public static void loopFiles(File file,List<File> jars){
		if (file.isDirectory()) {
			File[] tmps = file.listFiles();
			for (File tmp : tmps) {
				loopFiles(tmp, jars);
			}
		} else {
			if (file.getAbsolutePath().endsWith(".jar") || file.getAbsolutePath().endsWith(".zip")) {
				jars.add(file);
			}
		}
	}
}
