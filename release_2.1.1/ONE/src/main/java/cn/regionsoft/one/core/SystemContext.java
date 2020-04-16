package cn.regionsoft.one.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import cn.regionsoft.one.caches.LocalCacheUtil;
import cn.regionsoft.one.caches.RedisUtil;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.aop.ApplicationListener;
import cn.regionsoft.one.core.aop.BackendErrorLogInterceptor;
import cn.regionsoft.one.core.aop.LifecycleInterceptor;
import cn.regionsoft.one.core.auth.AdvancedSecurityProvider;
import cn.regionsoft.one.core.auth.BasicSecurityProvider;
import cn.regionsoft.one.properties.ConfigUtil;
import cn.regionsoft.one.rpc.client.RpcProxy;
import cn.regionsoft.one.rpc.client.MicrosvcManager;
import cn.regionsoft.one.rpc.common.ServerConstant;

public class SystemContext {
	private static final Logger logger = Logger.getLogger(SystemContext.class);
	private static SystemContext systemContext = new SystemContext();
	
	private SystemContext(){};
	
	public static SystemContext getInstance(){
		return systemContext;
	}
	
	private Map<String,H2OContext> contextsMap = new HashMap<String,H2OContext>();
	private boolean inited = false;
	public static final String DEFAULT_CONTEXT = "DefaultContext";
	
	
	/**
	 * rpc services consumer
	 */
	
	private MicrosvcManager microsvcManager;
	

	synchronized MicrosvcManager getMicrosvcManager() {
		if(microsvcManager==null) {
			String zookeeperConnectstr = ConfigUtil.getProperty(ServerConstant.RS_MICROSVC_ZOOKEEPERS);
			if(CommonUtil.isEmpty(zookeeperConnectstr)){
				throw new RuntimeException("rs.microsvc.zookeepers is not configured");
			}
			microsvcManager = new MicrosvcManager(zookeeperConnectstr);
		}
		return microsvcManager;
	}


	public void init(ContextConfig... contextConfigs){
		try{
			if(!inited){
				RedisUtil.init();
				
				Long start = System.currentTimeMillis();
				String lifecycleInterceptor = ConfigUtil.getProperty(ServerConstant.LIFECYCLE_INTERCEPTOR);
				
				inited = true;
				LifecycleInterceptor lifecycleInterceptorObj = null;
				/*String locale = ConfigUtil.getProperty(ServerConstant.LOCALE);
				if(!CommonUtil.isEmpty(locale)){
					if("Locale.EN".equals(locale)){
						Locale.setDefault(Locale.ENGLISH);
					}
					else if("Locale.CHINA".equals(locale)){
						Locale.setDefault(Locale.CHINA);
					}
					else{
						Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
					}
				}*/
				
				if(!CommonUtil.isEmpty(lifecycleInterceptor)){
					try {
						Class tmp = Class.forName(lifecycleInterceptor);
						if(tmp!=null)
							lifecycleInterceptorObj = (LifecycleInterceptor) tmp.newInstance();
					} catch (ClassNotFoundException e) {
						/*throw new RuntimeException(e);*/
					} catch (InstantiationException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
				if(lifecycleInterceptorObj!=null){
					lifecycleInterceptorObj.beforeInitContext();
				}
				
				for(ContextConfig tmp:contextConfigs){
					H2OContext context = new H2OContext(tmp,this);
					contextsMap.put(tmp.getContextName(), context);
				}
				//------------------------------------------------------------------------------------
				for(H2OContext context : contextsMap.values()){
					context.init();
				}

				//consumers
				if(this.microsvcManager!=null) {
					if(this.microsvcManager.getConsumerCount()>0)
						this.microsvcManager.startWatchServices();
					
					if(this.microsvcManager.getProviderCount()>0)
						this.microsvcManager.startMicroSvcServer();
					
				}
				
				/**
				 * init security begin
				 */
				String basicSecurityProviderStr = ConfigUtil.getProperty(ServerConstant.BASIC_SECURITY_PROVIDER);
				if(!CommonUtil.isEmpty(basicSecurityProviderStr)){
					try {
						Class providerClass = Class.forName(basicSecurityProviderStr);
						BasicSecurityProvider basicSecurityProvider = (BasicSecurityProvider) systemContext.getManagedBean(providerClass);
						systemContext.setBasicSecurityProvider(basicSecurityProvider);
					} catch (ClassNotFoundException e) {
						logger.warn(e);
					}
				}
				
				
				String advancedSecurityProviderStr = ConfigUtil.getProperty(ServerConstant.ADVANCED_SECURITY_PROVIDER);
				if(!CommonUtil.isEmpty(advancedSecurityProviderStr)){
					try {
						Class providerClass = Class.forName(advancedSecurityProviderStr);
						AdvancedSecurityProvider advancedSecurityProvider = (AdvancedSecurityProvider) systemContext.getManagedBean(providerClass);
						systemContext.setAdvancedSecurityProvider(advancedSecurityProvider);
					} catch (ClassNotFoundException e) {
						logger.warn(e);
					}
				}
				/**
				 * init security end
				 */
				if(lifecycleInterceptorObj!=null){
					lifecycleInterceptorObj.afterInitContext();
				}
				
				
				String backendErrorInterceptor = ConfigUtil.getProperty(ServerConstant.BACKEND_ERROR_INTERCEPTOR);
				if(!CommonUtil.isEmpty(backendErrorInterceptor)){
					try {
						Class tmp = Class.forName(backendErrorInterceptor);
						this.backendErrorLogInterceptor = (BackendErrorLogInterceptor) this.getManagedBean(tmp);
					} catch (ClassNotFoundException e) {
						logger.warn(e);
					}
				}
				logger.debug("init ApplicationListener");
				for(H2OContext context : contextsMap.values()){
					 Set<ApplicationListener> listeners = context.getApplicationListener();
					 for(ApplicationListener listener:listeners) {
						 listener.onApplicationInited(this);
					 }
				}
				
				logger.debug("init system context within ",System.currentTimeMillis()-start ," milliseconds");
			}
		}
		finally{
			SYSEnvSetup.releaseResource();
		}
	}
	
	private BackendErrorLogInterceptor backendErrorLogInterceptor ; 
	
	public BackendErrorLogInterceptor getBackendErrorLogInterceptor() {
		return backendErrorLogInterceptor;
	}

//	private boolean hasRemoteAnno(Class<?> classType){
//		Field[] fileds = classType.getDeclaredFields();
//		Autowired autowired = null;
//		for(Field field:fileds){
//			autowired = field.getDeclaredAnnotation(Autowired.class);
//			if(autowired==null) continue;
//			if(!CommonUtil.isEmpty(autowired.remoteEndPoint())){
//				return true;
//			}
//		}
//		return false;
//	}

	
//	private void initRPCSvcConsumer() {
//		String zookeeperConnectstr = ConfigUtil.getProperty(ServerConstant.REGISTERHOST_CONNECTSTR);
//		if(CommonUtil.isEmpty(zookeeperConnectstr)){
//			logger.debug("registerHost.conectstr not found, skip initRPCSvcConsumer ");
//			return;
//		}
//		
//		microsvcHub = new ServiceDiscovery(zookeeperConnectstr);
//		rpcProxy = new RpcProxy(microsvcHub);
//		
//	}
	

	public <T> T getManagedBean(Class<T> classType) {
		String cacheKey = LocalCacheUtil.GET_MANAGED_BEAN+classType.getName();
		T cached = (T) LocalCacheUtil.get(cacheKey);
		if(cached!=null)return cached;
		
		String contextName = CommonUtil.getTargetContextName(classType);
		if(contextName==null||contextName.trim().equals("")){
			contextName = DEFAULT_CONTEXT;
		}
		
		T result = getContext(contextName).getManagedBean(classType);
		LocalCacheUtil.put(cacheKey, result);
		return result;
	}
	
	public <T> T getManagedBean(String classFullName) throws ClassNotFoundException {
		String cacheKey = LocalCacheUtil.GET_MANAGED_BEAN+classFullName;
		T cached = (T) LocalCacheUtil.get(cacheKey);
		if(cached!=null)return cached;
		
		Class<T> classType = (Class<T>) Class.forName(classFullName,true,this.getClass().getClassLoader());
		
		T result = getManagedBean(classType);
		LocalCacheUtil.put(cacheKey, result);
		return result;
	}
	
	public H2OContext getContext(String contextName){
		return contextsMap.get(contextName);
	}

	public Map<String, H2OContext> getContextsMap() {
		return contextsMap;
	}
	
	/**
	 * release connections bound with current thread
	 */
	/*public void releaseResources(){
		Iterator<H2OContext> iterator = contextsMap.values().iterator();
		while(iterator.hasNext()){
			ConnectioManager.releaseConnection(iterator.next());
		}
	}*/
	
	/**
	 * authorization
	 */
	private AdvancedSecurityProvider advancedSecurityProvider;

	public AdvancedSecurityProvider getAdvancedSecurityProvider() {
		return advancedSecurityProvider;
	}

	public void setAdvancedSecurityProvider(AdvancedSecurityProvider securityProvider) {
		this.advancedSecurityProvider = securityProvider;
	}
	
	private BasicSecurityProvider basicSecurityProvider;

	public BasicSecurityProvider getBasicSecurityProvider() {
		return basicSecurityProvider;
	}

	public void setBasicSecurityProvider(BasicSecurityProvider basicSecurityProvider) {
		this.basicSecurityProvider = basicSecurityProvider;
	}

	
}
