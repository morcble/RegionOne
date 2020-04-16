package cn.regionsoft.one.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.MessageFormat;
import java.util.concurrent.ConcurrentHashMap;

import cn.regionsoft.one.assist.aop.interfaces.Interceptor;
import cn.regionsoft.one.assist.aop.interfaces.InterfaceProxyAssist;
import cn.regionsoft.one.common.Logger;
import cn.regionsoft.one.core.RequestInfoHolder;
import cn.regionsoft.one.core.auth.dto.RequestInfoDto;
import cn.regionsoft.one.rpc.common.ParaUtil;
import cn.regionsoft.one.rpc.common.RpcRequest;
import cn.regionsoft.one.rpc.common.RpcResponse;
import cn.regionsoft.one.web.core.ResProxy;

import io.netty.channel.Channel;


public class RpcProxy implements ResProxy{
	private static final Logger logger = Logger.getLogger(RpcProxy.class);
	
    private MicrosvcManager microsvcManager;
    //private Lock lock = new ReentrantLock();
    private RpcClientPool rpcClientPool= null;
    
    public RpcProxy(MicrosvcManager microsvcManager) {
        this.microsvcManager = microsvcManager;
        rpcClientPool = new RpcClientPool(2*Runtime.getRuntime().availableProcessors()+1);
    }
    public static String DELIMETER = ":";
    
    private boolean useJdkProxy = true;
    
    private static final String SERVICE_NOT_FOUND = "microsvcManager is null";
    private static final String NO_REMOTE_SERVICE_FOUND = "No remote service found in service registration server:{0}  , path : {1}";
    
    
    
    @SuppressWarnings("unchecked")
	public  <T> T create(Class<?> interfaceClass, final String registerPath) {
    	if(!useJdkProxy) {
    		try {
    			return (T) InterfaceProxyAssist.proxyInterface(interfaceClass,new Interceptor() {

					@Override
					public void beforeInvoke() {	
					}

					@Override
					public Object invoke(Class withinClass, String methodName, Class<?>[] parameterTypes,
							Object[] parameters) {
						try{
				    		RpcRequest request = new RpcRequest();
				    		/*RequestInfoDto requestInfoDto = RequestInfoHolder.getInstance().getRequestInfo();
				    		if(requestInfoDto!=null)
				    			request.setToken(RequestInfoHolder.getInstance().getRequestInfo().getToken());*/
				            request.setRequestId(RequestInfoHolder.getInstance().getRequestInfo().getRequestId());
				            request.setLocale(RequestInfoHolder.getInstance().getRequestInfo().getLocale());
				            request.setRegisterPath(withinClass.getName());
				            request.setMethodName(methodName);
				            //request.setParameterTypes(ParaUtil.paseParaTypesToString(parameterTypes));
				            
				            byte[] paraBytes = null;
				            
				            if(parameters!=null&&parameters.length>0) {
				            	
				            	paraBytes = ParaUtil.getBytes(parameters,parameterTypes);
				            	/*int paraCount = parameters.length;
				            	byte[][] objBytes = new byte[paraCount][];
				            	int totalLength = 0;
					            for(int i = 0 ; i <paraCount ; i++) {
					            	objBytes[i] =  ParaUtil.getBytes(parameters[i],parameterTypes[i]);
					            	totalLength+=objBytes[i].length;
					            }
					            
					            paraBytes = new byte[totalLength];
					            int destPos = 0;
					            for(int i = 0 ; i <paraCount ; i++) {
					            	System.arraycopy(objBytes[i], 0, paraBytes, destPos, objBytes[i].length);
					            	destPos+=objBytes[i].length;
					            }*/
					           // request.setParameters(paraBytes);
				            }
				            
				            
				           // request.setParameters(parameters);
				            if (microsvcManager == null) throw new Exception(SERVICE_NOT_FOUND);
				            
				            String serverAddress = microsvcManager.discover(registerPath);
				            
				            if(serverAddress==null){
				            	throw new Exception(MessageFormat.format(NO_REMOTE_SERVICE_FOUND, microsvcManager.getZkConectStr(),registerPath));
				            }
				            
				            RpcClient rpcClient = rpcClientPool.getClient(serverAddress);//一个client多个channel
				            Channel channel = rpcClient.send(request);
				            RpcResponse rpcResponse= rpcClient.getResponse(channel);
				            return rpcResponse.getResult();
				    	}
				    	catch(Throwable e){
				    		logger.error(e);
				    	}
				    	return null;
					}

					@Override
					public void afterInvoke() {
	
					}
    				
    			});
    		} catch (Exception e) {
    			logger.debug(e);
    			return null;
    		} 
    	}
    	else {
            return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new RemoteInvocationHandler(interfaceClass, registerPath, microsvcManager, rpcClientPool)
            );
        
    	}
    	
    }
}
