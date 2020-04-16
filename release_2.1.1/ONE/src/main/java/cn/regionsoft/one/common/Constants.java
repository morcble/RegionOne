package cn.regionsoft.one.common;

public class Constants {
	public static final int SERVER_DEFAULT_PORT = 80;
	public static final String USER_SESSION = "usersessioninfo";
	public static final String TOKEN = "token";
	
	public static int PAGE_SIZE = 15;
	public static int CACHE_PERIOD = 3600;
	public static int OBJECT_CACHE_PERIOD = 3600;

	public static final String DATE_FORMAT0 = "yyyy-MM-dd";
	public static final String DATE_FORMAT1 = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_FORMAT2 = "dd-MMM-yyyy";
	public static final String DATE_FORMAT3 = "yyyy   MM  dd";
	public static final String DATE_FORMAT4 = "yyyy.MM.dd";
	public static final String DATE_FORMAT5 = "yyyy    MM      dd";
	public static final String DATE_FORMAT6 = "yyyy-MM-dd HH:mm";
	//public static final String DATE_FORMAT7 = "yyyy-MMM-dd";
	public static final String TIME_FORMAT1 = "HH:mm";

	public static final String DATE_FORMAT10 = "dd-MMM-yyyy HH:mm";	  
	public static final String DATE_FORMAT11 = "dd/MMM/yyyy HH:mm";
	public static final String DATE_FORMAT12 = "dd/MMM/yyyy";
	
	public static final String DATE_FORMAT13 = "dd-MM-yyyy";
	public static final String DATE_FORMAT14 = "E, dd MMM yyyy HH:mm:ss z";
	public static final String DATE_FORMAT15 = "yyyyMMdd";
	public static final String DATE_FORMAT16 = "yyyy_MM_dd_HH_mm";
	
	public static final String UTF8 = "UTF-8";
	public static final String ISO_8859_1 = "ISO-8859-1";
	
	public static final String SYSTEM_SEPERATOR = System.getProperty("file.separator");
	public static String FILE_SERVER_ROOT = null;
	public static String SYSTEM_ROOT = null;
	
	public static final String PATH_DELIMETER = "/";
	
	/**
	 * silver config constants
	 */
	public static final String SILVER_HOST = "silver.host";
	public static final String SILVER_PORT = "silver.port";
	public static final String SILVER_GROUP_ID = "silver.groupId";
	public static final String SILVER_ITEM_ID = "silver.itemId";
	
	public static final String EMPTY_STR = "";
	public static final String SINGLE_QUOTE = "‘";
	public static final String DOUBLE_QUOTE = "\"";
	public static final String PERCENT = "%";
	
	public static final String MINUS = "-";
	public static final String OK = "Ok";
	public static final String SPACE_STR = " ";
	
	public static final String NEW_LINE = "\r\n";
	
	public static final String DOT = ".";
	public static final String DOT_SPLITER = "\\.";
	public static final String OCCUP_START = "${";
	public static final String OCCUP_END = "}";
	public static final String UNDER_LINE = "_";
	public static final String COLON = ":";
	
	public static final String GMT8  = "GMT-8:00";
	
	public static final String Y = "Y";
	public static final String N  = "N";
	public static final String ENCRYPT  = "encrypt";
	public static final String AES_KEY  = "aes.key";
	public static final String AES_IV  = "aes.iv";
	public static final String TRUE  = "true";
	public static final String POST  = "POST";
	public static final String AES_ENABLED  = "aes.enabled";
	public static final String DOLLAR = "$";
	public static final String SLASH = "/";
}
