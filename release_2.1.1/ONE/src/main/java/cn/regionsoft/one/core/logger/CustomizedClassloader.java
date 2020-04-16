package cn.regionsoft.one.core.logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
 

public class CustomizedClassloader extends URLClassLoader {// parasoft-suppress  SECURITY.WSC.CL

	public CustomizedClassloader(URL[] urls) {
        super(urls);
    }

    public CustomizedClassloader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
    
    
    public void addJarPath(String jarPath) throws MalformedURLException {
		File file = new File(jarPath);
		List<File> jars = new ArrayList<File>();
		loopFiles(file,jars);
		for(int i = 0 ; i<jars.size();i++){
			this.addURL(jars.get(i).toURI().toURL());
		}
    }
    
    public void addJar(URL url) throws MalformedURLException {
    	this.addURL(url);
    }
    
    
    public void loopFiles(File file,List<File> jars){
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
    
 /*   @Override
    public synchronized Class<?> findClass(String name) throws ClassNotFoundException{
    	if(loadOutSide(name)){
			return systemClassLoader.loadClass(name);
		}
		return super.findClass(name);
    }*/
    
  
	
   @Override
	public synchronized Class<?> loadClass(String name, boolean resolve)// parasoft-suppress  SECURITY.WSC.CLO
			throws ClassNotFoundException{
	   
		if(loadOutSide(name)){
			return systemClassLoader.loadClass(name);
		}
		return super.loadClass(name, resolve);
	}
	
    @Override
	 public Class<?> loadClass(String name) throws ClassNotFoundException {// parasoft-suppress  SECURITY.WSC.CLO
    	if(loadOutSide(name)){
				return systemClassLoader.loadClass(name);
		 }
		 return super.loadClass(name);
	 }
	 
	 private ClassLoader systemClassLoader;

	public void setSystemClassLoader(ClassLoader systemClassLoader) {
		this.systemClassLoader = systemClassLoader;
	}
	
	private boolean loadOutSide(String name){
		if(true)return false;
		
		if (name.indexOf("javax.activation") == -1 && name.indexOf("javax.mail") == -1
				&& name.indexOf("com.sun.mail") == -1 && name.indexOf("com.sun.activation") == -1
				&& name.indexOf("com.morcble") == -1) {
			return true;
		}
		else{
			return false;
		}
	}
 
}