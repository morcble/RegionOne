package cn.regionsoft.one.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import org.quartz.Scheduler;

import cn.regionsoft.one.annotation.Controller;
import cn.regionsoft.one.annotation.Job;
import cn.regionsoft.one.annotation.MicroConsumer;
import cn.regionsoft.one.annotation.MicroProvider;
import cn.regionsoft.one.annotation.tag.AfterAutowired;
import cn.regionsoft.one.annotation.tag.Autowired;
import cn.regionsoft.one.caches.aop.CacheListener;
import cn.regionsoft.one.caches.aop.CachesListener;
import cn.regionsoft.one.common.Constants;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.aop.AOPListener;
import cn.regionsoft.one.core.aop.ApplicationListener;
import cn.regionsoft.one.core.aop.impl.H2OAop;
import cn.regionsoft.one.core.aop.impl.TransactionListener;
import cn.regionsoft.one.core.controller.ControllerManager;
import cn.regionsoft.one.core.controller.RequestNodeWrapper;
import cn.regionsoft.one.core.dbconnection.ConnectionPool;
import cn.regionsoft.one.core.dbconnection.H2OConnection;
import cn.regionsoft.one.core.entity.CloudDBEntityManager;
import cn.regionsoft.one.core.entity.MongoEntityManager;
import cn.regionsoft.one.core.entity.SQLEntityManager;
import cn.regionsoft.one.core.threads.InitConnnectionPoolTask;
import cn.regionsoft.one.core.threads.ScanContextClassesTask;
import cn.regionsoft.one.rpc.client.MicrosvcManager;
import cn.regionsoft.one.rpc.client.RpcProxy;
import cn.regionsoft.one.rpc.common.ServerConstant;
import cn.regionsoft.one.schedule.ScheduleManager;
import cn.regionsoft.one.tool.ThreadPool;

public class H2OContext {
	private static final Logger logger = Logger.getLogger(H2OContext.class);
	/**
	 * XframeContextName
	 */
	private String contextName = null;
	
	/**
	 * store all the db connections
	 */
	private ConnectionPool connectionPool;
	
	/**
	 * framework class - class instance
	 */
	private Map<Class<?>,Object> classInstanceMap = new ConcurrentHashMap<Class<?>,Object>();
	
	/**
	 * ApplicationListener class - class instance
	 */
	private Set<ApplicationListener> applicationListener = new HashSet<ApplicationListener>();
	/*
	 * store all the class with framework annotation
	 */
	private AnnotatedClassHub annotatedClassHub = null;
	
	
	private EntityManager entityManager = null;
	
	private ControllerManager controllerManager = null;
	/*
	 * intercept class within XframeAnoType
	 */
	//private ClassProxy h2oContextInterceptor = new AopReflectInterceptor();
	private ClassProxy cglibInterceptor = new H2OAop();
	
	private ContextConfig config;
	
	/**
	 * 
	 */
	private List<AOPListener> aopListeners = new ArrayList<AOPListener>();

	private Set<Class<?>> needInstanceClass = null;
	
	private SystemContext systemContext = null;
	
	public H2OContext(ContextConfig config, SystemContext systemContext){
		this.config = config;
		this.systemContext = systemContext;
		this.contextName = config.getContextName();
		
		onCreate();
	}

	public ContextConfig getConfig() {
		return config;
	}
	

	public Set<ApplicationListener> getApplicationListener() {
		return applicationListener;
	}

	private void onCreate() {
		annotatedClassHub = new AnnotatedClassHub();
		HashSet<Class<?>> contextClasses = null;
		try{
			InitConnnectionPoolTask poolTask = new InitConnnectionPoolTask(this);
			Future<ConnectionPool> poolTaskResult  = ThreadPool.getInstance().submitQuickTask(poolTask);
			
			ScanContextClassesTask contextClassesTask = new ScanContextClassesTask(this);
			Future<HashSet<Class<?>>> contextClassesTaskResult = ThreadPool.getInstance().submitQuickTask(contextClassesTask);
			
			connectionPool = poolTaskResult.get();
			contextClasses = contextClassesTaskResult.get();
			if (connectionPool==null) logger.warn("Connection pool init failed");
			if (contextClasses==null) throw new Exception("System context scan failed");
		}
		catch(Exception e){
			logger.error(e);
		}
		needInstanceClass = annotatedClassHub.init(contextClasses,contextName);
	}
	
	void init(){
		if(DBType.CLOUDDB==config.getDbType()) {
			//init Entities
			entityManager = new CloudDBEntityManager(this);
			entityManager.init();
		}
		else if(DBType.MONGODB==config.getDbType()) {
			//init Entities
			entityManager = new MongoEntityManager(this);
			entityManager.init();
		}
		else {
			//init Entities
			entityManager = new SQLEntityManager(this);
			entityManager.init();
		}
		
		
		/**
		 * add aop listners from config
		 */
		aopListeners.add(new TransactionListener());//default listener from db transaction
		aopListeners.add(new CachesListener());
		aopListeners.add(new CacheListener());
		
		controllerManager = new ControllerManager(this);
		
		if(config.getAopListeners()!=null){
			for(AOPListener aopListener:config.getAopListeners()){
				aopListeners.add(aopListener);
			}
		}
		initClassesInstance(needInstanceClass);
	}
	
	public RequestNodeWrapper getMatchedRequestNode(String requestURI) throws Exception{
		return controllerManager.getMatchedRequestNode(requestURI);
	}


	public EntityManager getEntityManager() {
		return entityManager;
	}

	private void initClassesInstance(Set<Class<?>> xframeManagedClassSet) {
		for(Class<?> tmp:xframeManagedClassSet){
			try {
				initIfNoManagedBean(tmp,null,null);
			} catch (Exception e) {
				logger.error(e);
			} 
		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> T  initIfNoManagedBean(Class<T> contextManagedClass, Field field,Object parentInstance) throws Exception {
		if(contextManagedClass.isInterface()){
			if(contextManagedClass.isAnnotation()) {
				return null;
			}
			
			MicroConsumer microConsumer = field.getAnnotation(MicroConsumer.class);
			if(microConsumer!=null) {
				return (T) createRemoteProxy(field,contextManagedClass,microConsumer);
			}
			
			Autowired autowired = field.getAnnotation(Autowired.class);
			if(autowired==null){
				throw new Exception("not able to create instance,"+contextManagedClass.getName());
			}
			contextManagedClass = autowired.targetClass();
		}
		
		String tmpName = CommonUtil.getTargetContextName(contextManagedClass);
		H2OContext h2oContext = systemContext.getContext(tmpName);
		if(h2oContext==null){
			throw new RuntimeException("Context not found :"+tmpName + " - "+contextManagedClass.getName());
		}
		Map<Class<?>,Object> tmpMap = h2oContext.getClassInstanceMap();
		Object instance = tmpMap.get(contextManagedClass);
		if(instance!=null) return (T) instance;

		//dao & service &resource & controller & Component--------------------------------------------------------------------------
		try {
			//logger.debug("aspect point on class ",contextManagedClass);
			MicroProvider microProvider = contextManagedClass.getAnnotation(MicroProvider.class);
			if(microProvider!=null) {
				Class<?>[] interfaces = contextManagedClass.getInterfaces();
				if(interfaces.length==0) {
					throw new RuntimeException("no interface found for mocro provider. class:"+contextManagedClass.getName());
				}
				else if(interfaces.length>1) {
					throw new RuntimeException("more than one interface found for mocro provider. class:"+contextManagedClass.getName());
				}
				instance = contextManagedClass.newInstance();
				registerProvider(interfaces[0],microProvider, instance);
			}
			else {
				if(contextManagedClass.isAnnotationPresent(Controller.class)){
					instance = contextManagedClass.newInstance();
				}
				else {
					instance = cglibInterceptor.proxyCglibClass(contextManagedClass);
				}
			}

//			Resource resource = contextManagedClass.getAnnotation(Resource.class);
//			if(resource!=null){
//				result = contextManagedClass.newInstance();
//				if(resource.remoteInterface()!=Object.class){
//					systemContext.registerRpc(resource.remoteInterface(), result);
//				}
//			}
//			else {
//				if(contextManagedClass.isAnnotationPresent(Controller.class)
//						//||contextManagedClass.isAnnotationPresent(QuickEventListener.class)
//					) {
//					result = contextManagedClass.newInstance();
//				}
//				else {
//					result = cglibInterceptor.proxyCglibClass(contextManagedClass);
//				}
//				
//			}
			
			/**
			 * init Autowired
			 */
			autowired(contextManagedClass,instance,parentInstance);
			
			/**
			 * init schedule jobs
			 */
			initJobs(contextManagedClass,instance,parentInstance);

			//classInstanceMap
			try{
				Method[] methods = contextManagedClass.getDeclaredMethods();
				for(Method method:methods){
					if(method.isAnnotationPresent(AfterAutowired.class)){
						method.setAccessible(true);
						method.invoke(instance);
					}
				}
			}
			catch(Exception e){
				logger.error(e);
			}
			
		
			//aopCglibInterceptor.proxyCglibClass(xframeManagedClass);
			
			tmpMap.put(contextManagedClass,instance);	
			
			//加入监听应用接口的listener
			if(instance instanceof ApplicationListener) {
				h2oContext.getApplicationListener().add((ApplicationListener)instance);
			}
		} catch (Exception e) {
			logger.error(e);
		} 
		
		return (T) instance;
	}
	
	private void initJobs(Class<?> contextManagedClass, Object result, Object refObj) throws Exception {
		Method[] methods = contextManagedClass.getDeclaredMethods();
		for(Method method:methods) {
			Job job = method.getAnnotation(Job.class);
			if(job!=null) {
				//Scheduler scheduler = systemContext.getScheduler();
				ScheduleManager scheduleManager = ScheduleManager.getInstance();
				scheduleManager.initNewJob(job,method,result);
			}
		}
	}

	public Map<Class<?>, Object> getClassInstanceMap() {
		return classInstanceMap;
	}


	private void autowired(Class<?> xframeManagedClass,Object target,Object refObj) {
		Field[] fields = xframeManagedClass.getDeclaredFields();
		for(Field field:fields){
			if((field.isAnnotationPresent(Autowired.class)||field.isAnnotationPresent(MicroConsumer.class))){
				try {
					if(!Modifier.isFinal(field.getModifiers())) {
						
						boolean filled = false;
						if(refObj!=null) {
							String refObjClassName = refObj.getClass().getName();
							int index = refObjClassName.indexOf("$$");
							if(index!=-1) {
								refObjClassName = refObjClassName.substring(0,index);
								if(refObjClassName.equals(field.getType().getName())) {
									field.setAccessible(true);
									field.set(target, refObj);
									filled = true;
								}
							}
						}
						if(!filled) {
							Object tmp = initIfNoManagedBean(field.getType(),field,target);
							field.setAccessible(true);
							field.set(target, tmp);
						}
					}					
				} catch (Exception e) {
					logger.error(e);
				}
			}
		}
		
		Class<?> parentClass = xframeManagedClass.getSuperclass();
		if(parentClass!=Object.class){
			autowired(parentClass,target,refObj);
		}
		
	}
	//private ReentrantLock getManagedBeanlock = new ReentrantLock();
	private final Semaphore semp = new Semaphore(1);
	<T> T  getManagedBean(Class<T> xframeManagedClass) {
		Object result = classInstanceMap.get(xframeManagedClass);
		if(result!=null) return (T) result;
		try{
			semp.acquire();
			return initIfNoManagedBean(xframeManagedClass,null,null);
		}
		catch(Exception e){
			logger.error(e);
			return null;
		}
		finally{
			semp.release();
		}
	}
	
	public H2OConnection getConnectionFromPool(){
		return connectionPool.getConnectionFromPool();
	}
	public ConnectionPool getConnectionPool() {
		return connectionPool;
	}


	public H2OConnection newConnection() throws Exception {
		return connectionPool.newConnection();
	}
	
	public int poolAvailableSize(){
		return connectionPool.avaliable();
	}
	public AnnotatedClassHub getAnnotatedClassHub() {
		return annotatedClassHub;
	}

	public String getContextName() {
		return contextName;
	}
	
	public void addAopListener(AOPListener aopListener) {
		aopListeners.add(aopListener);
	}
	
	public List<AOPListener> getAopListeners() {
		return aopListeners;
	}
	
	/**
	 * 为本地含有MicroConsumer注解的接口,创建动态代理类,通过rpc服务访问远程类方法
	 * @param field
	 * @param interfaceClass
	 * @param microConsumer 
	 * @return
	 */
	private Object createRemoteProxy(Field field,Class interfaceClass, MicroConsumer microConsumer){
		MicrosvcManager microsvcManager = systemContext.getMicrosvcManager();
		microsvcManager.plusConsumer();
		
		RpcProxy rpcProxy = microsvcManager.getRpcProxy();
		if(rpcProxy==null) {
			rpcProxy = new RpcProxy(microsvcManager);
			microsvcManager.setRpcProxy(rpcProxy);
		}
		
		String registerEndpoint = ServerConstant.RS_MICROSVC_REG_CONTEXT+"/"+microConsumer.group()+"/"+interfaceClass.getName()+"/"+microConsumer.version()+"/providers";
		microsvcManager.registerConsumer(registerEndpoint);
		return rpcProxy.create(field.getType(),registerEndpoint);
	}

	public void registerProvider(Class remoteInterface,MicroProvider microProvider,Object resourceBean){
		MicrosvcManager microsvcManager = systemContext.getMicrosvcManager();
		microsvcManager.plusProvider();
		
		String registerEndpoint = ServerConstant.RS_MICROSVC_REG_CONTEXT+"/"+microProvider.group()+"/"+remoteInterface.getName()+"/"+microProvider.version()+"/providers";
		microsvcManager.getRpcHandlerMap().put(registerEndpoint, resourceBean);
	}

}
