package cn.regionsoft.one.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import cn.regionsoft.one.core.CommonUtil;

public class PropertiesFileMeta {
	private Properties properties;
	private String filePath;
	private long lastModified;

	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public long getLastModified() {
		return lastModified;
	}
	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}
	public void loadProperties(File tmp) {
		Properties newP = new Properties();
		InputStream is = null;
		InputStreamReader isr = null;
		try{
			isr=new InputStreamReader(new FileInputStream(tmp),"utf-8");
			newP.load(isr);
			properties = newP;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			CommonUtil.closeQuietly(isr);
			CommonUtil.closeQuietly(is);
		}
	}
	
	public String getProperty(String key){
		return properties.getProperty(key);
	}
	public String getProperty(String key,String defaultValue){
		return properties.getProperty(key, defaultValue);
	}
	public Properties getProperties(){
		return properties;
	}
	
	public void reload(File file) {
		loadProperties(file);
	}
	
}
