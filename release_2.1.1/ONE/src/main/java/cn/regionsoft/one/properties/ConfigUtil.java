package cn.regionsoft.one.properties;

import java.util.Properties;
import cn.regionsoft.one.core.CommonUtil;
import cn.regionsoft.one.test.Promise;
import com.cnautosoft.silver.client.PropertiesManager;

public class ConfigUtil {
	
	private static Properties props;
	private static boolean silverConfigExsit;

	public static void setProps(Properties props) {
		ConfigUtil.props = props;
	}

	public static String getProperty(String key) {
		String val = null;
		if(props!=null)
			val = props.getProperty(key);
		if(CommonUtil.isEmpty(val) && silverConfigExsit) {
			val = PropertiesManager.getInstance().getProperty(key);
		}		
		if(CommonUtil.isEmpty(val)) {
			val = System.getProperty(key);
		}
		if(CommonUtil.isEmpty(val)) {
			val = System.getenv(key);
		}
		return val;
	}
	
	public static Integer getPropertyAsInteger(String key) {
		String val = getProperty(key);
		if(val==null) return null;
		return Integer.parseInt(val);
	}
	
	public static String getProperty(String key ,String defaultVal) {
		String val = getProperty(key);
		if(CommonUtil.isEmpty(val)) {
			val = defaultVal;
		}
		return val;
	}
	
	public static String getPropertyAndPromiseNotNull(String key) {
		return Promise.notNull(getProperty(key), key+" is not configured");
	}
	
	public static String getPropertyAndPromiseNotEmpty(String key) {
		return Promise.notEmpty(getProperty(key), key+" is not configured");
	}

	public static boolean isSilverConfigExsit() {
		return silverConfigExsit;
	}

	public static void setSilverConfigExsit(boolean silverConfigExsit) {
		ConfigUtil.silverConfigExsit = silverConfigExsit;
	}

}
