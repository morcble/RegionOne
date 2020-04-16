package cn.regionsoft.one.rpc.common;

import cn.regionsoft.one.properties.ConfigUtil;

/**
 * 常量
 *
 * @author  
 * @since 1.0.0
 */
public interface ServerConstant {

	public final static int ZK_SESSION_TIMEOUT = 5000;
    
	public final static String SERVER_TYPE = "serverType";
	public final static String LIFECYCLE_INTERCEPTOR = "lifecycle.interceptor";
	public final static String BACKEND_ERROR_INTERCEPTOR = "backend.error.interceptor";
	
	public final static String SHOW_SQL = "show.sql";
	
    public final static String RS_MICROSVC_ZOOKEEPERS = "rs.microsvc.zookeepers";
    public final static String RS_MICROSVC_ENDPOINT = "rs.microsvc.endpoint";
    public final static String RS_MICROSVC_REG_CONTEXT = "/regionsoft/microsvc";
    public final static String LOCALE = "locale";
    
    public final static String REGION_TOKEN ="_token";
    public final static String REGION_LOGIN ="_region_login";
    public final static String REGION_VERIFY ="_verify";
    
    
    
    public final static String SECURITY_FILTER = "security.filter";
    public final static String BASIC_SECURITY_PROVIDER = "basic.security.provider";
    public final static String ADVANCED_SECURITY_PROVIDER = "advanced.security.provider";
    public final static String JWT_SIGNATURE = "jwt.signature";
    public final static String TOKEN_RENEW_GAP = "token.renew.gap";
    
    
  //session timeout config
    public static final Long SESSION_EXPIRE_GAP = Long.valueOf(ConfigUtil.getProperty("session.timeout", "600000"));//milliseconds
  	public static final Long RENEW_GAP  = SESSION_EXPIRE_GAP/2L;//millisecond
	
}