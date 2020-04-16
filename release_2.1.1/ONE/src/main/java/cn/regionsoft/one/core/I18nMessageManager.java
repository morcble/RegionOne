package cn.regionsoft.one.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import cn.regionsoft.one.common.Logger;

public class I18nMessageManager {
	public static Logger logger = Logger.getLogger(I18nMessageManager.class);
	
	//language - properties
	private static Map<String,Map<String,String>> propertiesMap = new HashMap<String,Map<String,String>>();
	
	private static final String DEFAULT = "default";
	
	public static void loadI18nMessage(){
		try{
			URL i18nFolderUrl = I18nMessageManager.class.getClassLoader().getResource("i18n");
			if(i18nFolderUrl==null){
				logger.warn("No i18n configuration found");
			}
			else{
				File i18nFolder = new File(i18nFolderUrl.getFile());
				for(File languageFolder:i18nFolder.listFiles()){
					if(languageFolder.isFile())continue;
					
					Map<String,String> languageProperties = new HashMap<String,String>();
					for(File file:languageFolder.listFiles()){
						Properties tmpProperties = loadProperties(file);
						Iterator<Entry<Object, Object>> iterator = tmpProperties.entrySet().iterator();
						while(iterator.hasNext()) {
							Entry<Object, Object> tmpEntry = iterator.next();
							languageProperties.put((String)tmpEntry.getKey(),(String)tmpEntry.getValue());
						}
					}
					
					propertiesMap.put(languageFolder.getName(), languageProperties);
				}
			}
		}
		catch(Exception e){
			logger.error(e);
		}
	}
	
	public static String getMessage(String key){
		Map<String,String> languageProperties = propertiesMap.get(DEFAULT);
		if(languageProperties==null){
			logger.error("No i18n configuration found");
		}
		
		return languageProperties.get(key);
	}
	
	public static String getMessageWithDefault(String key,String defaultMsg){
		Map<String,String> languageProperties = propertiesMap.get(DEFAULT);
		if(languageProperties==null){
			logger.error("No i18n configuration found");
		}
		
		String result = languageProperties.get(key);
		if(CommonUtil.isEmpty(result)) {
			return defaultMsg;
		}
		
		return result;
	}
	
	public static String getMessage(String key,String localeStr){
		Map<String,String> languageProperties = propertiesMap.get(localeStr);
		if(languageProperties==null){
			languageProperties = propertiesMap.get(DEFAULT);
		}
		if(languageProperties==null){
			logger.error("No i18n configuration found");
		}
		
		return languageProperties.get(key);
	}
	
	private static Properties loadProperties(File tmp) throws Exception {
		Properties properties = new Properties();
		InputStream is = null;
		InputStreamReader isr = null;
		try{
			isr=new InputStreamReader(new FileInputStream(tmp),"utf-8");
			properties.load(isr);
			return properties;
		}
		catch(Exception e){
			throw e;
		}
		finally{
			CommonUtil.closeQuietly(isr);
			CommonUtil.closeQuietly(is);
		}
	}
}
